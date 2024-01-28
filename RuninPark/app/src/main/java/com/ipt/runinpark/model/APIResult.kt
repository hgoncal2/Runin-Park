package com.ipt.runinpark.model

import com.google.gson.annotations.SerializedName

data class APIResult (
    @SerializedName("Code")
    val code: String?,
    @SerializedName("Description")
    val description: String?
)