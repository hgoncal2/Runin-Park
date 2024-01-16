package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class Photo(
    @SerializedName("Path")
    val path: String

)