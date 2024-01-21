package com.example.myapplication.retrofit.service

import com.example.myapplication.model.Photo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PhotoService {


    @POST("photos/user")

    @Multipart
    fun uploadPhoto(@Part image: MultipartBody.Part?, @Header("auth") token: String?): Call<Photo>



}