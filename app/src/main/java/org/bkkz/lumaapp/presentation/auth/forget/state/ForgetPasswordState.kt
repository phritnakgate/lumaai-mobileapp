package org.bkkz.lumaapp.presentation.auth.forget.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class ForgetPasswordState (
    val email: String? = null,
    val isEmailValid: Boolean = true,
    val serviceState: ServiceState = ServiceState.IDLE,
)