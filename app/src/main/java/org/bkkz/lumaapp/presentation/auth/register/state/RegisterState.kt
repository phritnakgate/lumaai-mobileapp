package org.bkkz.lumaapp.presentation.auth.register.state

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String? = null) : RegisterState()
    data class Error(val message: String) : RegisterState()
}