package com.example.artsyandroidapp.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class UserDto(val userId: String, val username: String, val email: String, val avatarUrl: String)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: Map<String,String>): UserDto      // cookie set

    @GET("auth/me")
    suspend fun me(): UserDto                                        // uses cookie

    @POST("auth/logout")
    suspend fun logout()
    @POST("auth/register")
    suspend fun register(@Body body: Map<String,String>): UserDto

    @DELETE("auth/delete-account")
    suspend fun deleteAccount()

}

class AuthRepository(private val api: AuthApi) {
    private val _user = MutableStateFlow<UserDto?>(null)
    val  user : StateFlow<UserDto?> = _user          // <- expose flow to the UI
    fun currentUser(): UserDto? = _user.value

    suspend fun tryRestoreSession(): Boolean = withContext(Dispatchers.IO) {
        Log.d("AuthRepo", "Calling /auth/me")
        runCatching { api.me() }
            .onSuccess {
                Log.d("AuthRepo", "Session restore success")
                _user.value = it
            }
            .onFailure { Log.e("AuthRepo", "Session restore failed", it) }
            .isSuccess
    }

    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { api.login(mapOf("email" to email, "password" to password)) }
            .onSuccess { _user.value = it }
            .isSuccess
    }

    suspend fun register(name: String, email: String, pwd: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { api.register(mapOf("username" to name, "email" to email, "password" to pwd)) }
            .onSuccess { _user.value = it }
            .isSuccess
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        runCatching { api.logout() }
        _user.value = null
    }

    suspend fun deleteAccount() = withContext(Dispatchers.IO) {
        runCatching { api.deleteAccount() }
        _user.value = null
    }
}

