package org.bkkz.lumaapp.presentation.main.setting.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class SettingsState(
    val isConnectedToCalendar: Boolean = false,
    val isLoginViaGoogle : Boolean = false,
    val googleCalendarEmail : String? = null,
    val serviceState: ServiceState = ServiceState.IDLE
)
