package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.util.Date

data class Run(
    @SerializedName("RunId")
    val runId: Int,
    @SerializedName("Distance")
    val distance: Double?,
    @SerializedName("Time")
    val time: LocalDateTime?,
    @SerializedName("CreatedDate")
    val createdDate: Date,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("GroupId")
    val groupId: Int,
    @SerializedName("PhotoPath")
    var runPhoto : String?,
    @SerializedName("UserPhotoPath")
    var userPhoto : String?,
    @SerializedName("Username")
    var username : String?,

)