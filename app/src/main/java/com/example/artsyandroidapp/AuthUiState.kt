package com.example.artsyandroidapp

import android.util.Log
import com.example.artsyandroidapp.auth.AuthRepository
import com.example.artsyandroidapp.auth.UserDto
import com.example.artsyandroidapp.net.NetModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AuthUiState(private val repo: AuthRepository) {
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    private val _isRestoring = MutableStateFlow(true)
    val isRestoring: StateFlow<Boolean> = _isRestoring

    private var navigateToLogin: (() -> Unit)? = null

    fun setNavigateToLoginCallback(cb: () -> Unit) {
        navigateToLogin = cb
    }

    fun navigateToLogin() {
        navigateToLogin?.invoke()
    }

    suspend fun tryRestoreSession(): Boolean = withContext(Dispatchers.IO) {
        Log.d("AuthUiState", "Start restoring session")
        _isRestoring.value = true
        val ok = try {
            val success = repo.tryRestoreSession()
            _user.value = repo.currentUser()
            success
        } catch (e: Exception) {
            Log.e("AuthUiState", "Session restore failed", e)
            false
        } finally {
            _isRestoring.value = false
        }
        Log.d("AuthUiState", "tryRestoreSession(): success=$ok, user=${_user.value}")
        return@withContext ok
    }

    suspend fun login(email: String, pass: String): Boolean {
        val success = repo.login(email, pass)
        if (success) {
            _user.value = repo.currentUser()
            _snackbar.value = "Logged in successfully"
        }
        return success
    }

    suspend fun register(name: String, email: String, pass: String): Boolean {
        val success = repo.register(name, email, pass)
        if (success){
            _user.value = repo.currentUser()
            _snackbar.value = "Registered successfully"
        }
        return success
    }

    suspend fun logout() {
        repo.logout()
        NetModule.cookieJar.clear()
        _user.value = null
        _snackbar.value = "Logged out successfully"
    }

    suspend fun deleteAccount() {
        repo.deleteAccount()
        NetModule.cookieJar.clear()
        _user.value = null
        _snackbar.value = "Deleted user successfully"
    }

    fun onSnackShown() {
        _snackbar.value = null
    }
}