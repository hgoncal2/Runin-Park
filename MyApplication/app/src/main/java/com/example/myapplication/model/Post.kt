package com.example.myapplication.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Post(
    @SerializedName("PostId")
    val postId: Int,
    @SerializedName("Text")
    val text: String?,
    @SerializedName("CreatedDate")
    val createdDate: Date,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("GroupId")
    val groupId: Int,
    @SerializedName("UserPhotoPath")
    var profilePhoto : String?,
    @SerializedName("PostPhotoPath")
    var postPhoto : String?,
    @SerializedName("Username")
    var username : String?
)