package org.bkkz.lumaapp.data.remote

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("result")
    val result : String? = null,
    @SerializedName("results")
    val results : List<T>? = null,
    @SerializedName("error")
    val error : String? = null,
    @SerializedName("errors")
    val errors : List<T>? = null,
)