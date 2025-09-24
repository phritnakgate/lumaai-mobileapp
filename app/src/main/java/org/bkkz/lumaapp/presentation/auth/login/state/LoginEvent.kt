package org.bkkz.lumaapp.presentation.auth.login.state

sealed class LoginEvent {
    object Idle : LoginEvent()
    object Loading : LoginEvent()
    data class Success(val email: String? = null) : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}