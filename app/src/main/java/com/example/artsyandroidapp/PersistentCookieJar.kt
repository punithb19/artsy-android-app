@file:OptIn(ExperimentalSerializationApi::class)
package com.example.artsyandroidapp

import android.content.SharedPreferences
import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.time.Instant

class PersistentCookieJar(
    private val prefs: SharedPreferences,
    private val json : Json = Json { encodeDefaults = true }
) : CookieJar {

    private val key = "cookies"

    /** in‑memory copy to avoid parsing JSON on each request */
    private val cache = loadFromPrefs().toMutableList()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // drop expired cookies first
        val now = Instant.now().epochSecond
        cache.removeAll { it.expiresAt < now }
        saveToPrefs()
        val matching = cache
            .map { it.toOkHttp() }
            .filter { it.matches(url) }

        Log.d("CookieJar", "Sending cookies to ${url.host}: $matching")

        return matching
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = Instant.now().epochSecond
        // overwrite cookies with same name+domain+path
        cookies.forEach { newC ->
            cache.removeAll { it.sameCookie(newC) || it.expiresAt < now }
            cache += SerializableCookie(newC)
        }
        saveToPrefs()
    }

    /* ---------- helpers ---------- */

    @Serializable
    data class SerializableCookie(
        val name: String,
        val value: String,
        val domain: String,
        val path: String,
        val expiresAt: Long,
        val secure: Boolean,
        val httpOnly: Boolean
    ) {
        constructor(cookie: Cookie) : this(
            cookie.name, cookie.value, cookie.domain, cookie.path,
            cookie.expiresAt / 1000, cookie.secure, cookie.httpOnly
        )

        fun toOkHttp(): Cookie = Cookie.Builder()
            .name(name).value(value).domain(domain).path(path)
            .expiresAt(expiresAt * 1000)
            .apply { if (secure) secure() else this }
            .apply { if (httpOnly) httpOnly() else this }
            .build()

        fun sameCookie(other: Cookie) =
            name == other.name && domain == other.domain && path == other.path
    }

    private fun loadFromPrefs(): List<SerializableCookie> =
        prefs.getString(key, null)
            ?.let { json.decodeFromString<List<SerializableCookie>>(it) }
            ?: emptyList()

    private fun saveToPrefs() {
        prefs.edit().putString(key, json.encodeToString(cache)).commit()
        Log.d("CookieJar", "Saved cookies: ${json.encodeToString(cache)}")
    }

    fun clear() {
        cache.clear()
        prefs.edit().remove(key).apply()
        Log.d("CookieJar","All cookies cleared")
    }
}
