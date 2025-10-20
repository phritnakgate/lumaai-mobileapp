package org.bkkz.lumaapp.presentation.main.report.state

import org.bkkz.lumaapp.data.entity.report_history.ReportHistory
import org.bkkz.lumaapp.util.enums.ServiceState

data class ReportActivityState(
    val serviceState: ServiceState = ServiceState.IDLE,
    val recentGeneratedFilePath : String? = null,
    val reportList : List<ReportHistory>? = null,
    val isDeleteMode : Boolean = false,
)
