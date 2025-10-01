package org.bkkz.lumaapp.presentation.main.report.state

import android.content.Context

sealed class ReportActivityEvent {
    data class OnGenerateMonthlyReport(val context: Context, val reportYM : String) : ReportActivityEvent()
}