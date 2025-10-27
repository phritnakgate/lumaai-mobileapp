package org.bkkz.lumaapp.presentation.main.setting.state

import org.bkkz.lumaapp.util.enums.ServiceState
import java.util.Locale

data class SettingsState(
    val isConnectedToCalendar: Boolean = false,
    val isLoginViaGoogle : Boolean = false,
    val googleCalendarEmail : String? = null,
    val currentLanguage : String? = Locale.getDefault().language,
    val serviceState: ServiceState = ServiceState.IDLE
)
