package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("City")
    val city: String,
    @SerializedName("CreatedDate")
    val createdDate: String,
    @SerializedName("GroupId")
    val groupId: Int,
    @SerializedName("Name")
    val name: String,
    @SerializedName("OwnerId")
    val ownerId: Int,
    @SerializedName("PhotoPath")
    var groupPhoto : String?
)