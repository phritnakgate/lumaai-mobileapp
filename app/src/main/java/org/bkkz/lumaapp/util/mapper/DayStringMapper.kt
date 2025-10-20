package org.bkkz.lumaapp.util.mapper

import android.content.Context
import org.bkkz.lumaapp.R

object DayStringMapper {
    private val resourceMap: Map<String, Int> by lazy {
        mapOf(
            "" to R.string.sun,
            "" to R.string.mon,
            "" to R.string.tue,
            "" to R.string.wed,
            "" to R.string.thu,
            "" to R.string.fri,
            "" to R.string.sat
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