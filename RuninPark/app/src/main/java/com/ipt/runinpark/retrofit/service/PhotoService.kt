package com.ipt.runinpark.retrofit.service

import com.ipt.runinpark.model.Photo
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoService {

//Dá upload da foto de perfil do utilizador
    @POST("photos/users")
    @Multipart
    fun uploadPhoto(@Part image: MultipartBody.Part?, @Header("auth") token: String?): Call<Photo>
//Dá upload da foto do grupo
    @POST("photos/{groupId}")
    @Multipart
    fun uploadGroupPhoto(@Part image: MultipartBody.Part?, @Header("auth") token: String?, @Path("groupId") groupId: Int): Call<Photo>



}