package org.bkkz.lumaapp.data.entity.report_history

import com.google.gson.annotations.SerializedName

data class ReportHistory(
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("url")
    val url: String,
    val isCached: Boolean = false,
)
