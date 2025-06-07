package com.example.artsyandroidapp.net

import android.content.Context
import com.example.artsyandroidapp.PersistentCookieJar
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// NetModule.kt
object NetModule {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    lateinit var cookieJar: PersistentCookieJar
        private set
    private lateinit var client: OkHttpClient
    lateinit var retrofit: Retrofit
        private set

    /** Call exactly once in Application (or from ServiceLocator.init) */
    fun init(ctx: Context) {
        val prefs = ctx.getSharedPreferences("cookies", Context.MODE_PRIVATE)
        cookieJar = PersistentCookieJar(prefs)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                )
            )
            .build()
    }
}

