package org.bkkz.lumaapp.presentation.auth.forget.state

sealed class ForgetPasswordEvent {
    data class OnEmailChange(val email: String) : ForgetPasswordEvent()
    data object SubmitRequest : ForgetPasswordEvent()
}