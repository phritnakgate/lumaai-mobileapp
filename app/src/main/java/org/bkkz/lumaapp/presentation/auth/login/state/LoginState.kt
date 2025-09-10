package org.bkkz.lumaapp.presentation.auth.login.state

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val message: String? = null) : LoginState()
    data class Error(val message: String) : LoginState()
}