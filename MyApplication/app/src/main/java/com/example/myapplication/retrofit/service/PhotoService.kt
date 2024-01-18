package com.example.myapplication.retrofit.service

import com.example.myapplication.model.Photo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PhotoService {


    @POST("photos")
    @Headers("auth:{}")
    @Multipart
    fun uploadPhoto(@Part image: MultipartBody.Part?, @Query("auth") auth: String?): Call<Photo>



}