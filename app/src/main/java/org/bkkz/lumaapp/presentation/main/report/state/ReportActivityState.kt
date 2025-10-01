package org.bkkz.lumaapp.presentation.main.report.state

import org.bkkz.lumaapp.util.enums.ServiceState

data class ReportActivityState(
    val serviceState: ServiceState = ServiceState.IDLE,
    val recentGeneratedFilePath : String? = null
)
