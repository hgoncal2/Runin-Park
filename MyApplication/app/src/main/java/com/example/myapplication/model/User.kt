package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class User(
    @SerializedName("Address")
    var address: String?,
    @SerializedName("BirthDate")
    var birthDate: Date?,
    @SerializedName("Height")
    var height: Double?,
    @SerializedName("LastName")
    var lastName: String?,
    @SerializedName("Name")
    var name: String?,
    @SerializedName("UserId")
    val userId : Int?,
    @SerializedName("Username")
    val username: String?,
    @SerializedName("Weight")
    var weight: Double?,
    var token : Token?

)