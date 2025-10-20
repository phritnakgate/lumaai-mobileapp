package org.bkkz.lumaapp.util.mapper

import android.content.Context
import org.bkkz.lumaapp.R

object MonthStringMapper {
    private val resourceMap: Map<String, Int> by lazy {
        mapOf(
            "month_1_full" to R.string.month_1_full,
            "month_1_cut" to R.string.month_1_cut,
            "month_2_full" to R.string.month_2_full,
            "month_2_cut" to R.string.month_2_cut,
            "month_3_full" to R.string.month_3_full,
            "month_3_cut" to R.string.month_3_cut,
            "month_4_full" to R.string.month_4_full,
            "month_4_cut" to R.string.month_4_cut,
            "month_5_full" to R.string.month_5_full,
            "month_5_cut" to R.string.month_5_cut,
            "month_6_full" to R.string.month_6_full,
            "month_6_cut" to R.string.month_6_cut,
            "month_7_full" to R.string.month_7_full,
            "month_7_cut" to R.string.month_7_cut,
            "month_8_full" to R.string.month_8_full,
            "month_8_cut" to R.string.month_8_cut,
            "month_9_full" to R.string.month_9_full,
            "month_9_cut" to R.string.month_9_cut,
            "month_10_full" to R.string.month_10_full,
            "month_10_cut" to R.string.month_10_cut,
            "month_11_full" to R.string.month_11_full,
            "month_11_cut" to R.string.month_11_cut,
            "month_12_full" to R.string.month_12_full,
            "month_12_cut" to R.string.month_12_cut
        )
    }

    fun getResourceId(resName: String): Int? {
        return resourceMap[resName]
    }

    fun getString(context: Context, resName: String): String? {
        return getResourceId(resName)?.let { id ->
            context.getString(id)
        }
    }
}