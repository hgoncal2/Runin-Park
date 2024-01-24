package com.example.myapplication.retrofit.service

import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Post
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {

    @POST("groups/{groupId}/posts")
    fun createPostNoImg(@Header("auth") token: String?, @Path("groupId") groupId:Int,@Query("text") text : String?): Call<APIResult>

    @POST("groups/{groupId}/posts")
    @Multipart
    fun createPost(@Part image: MultipartBody.Part?,@Header("auth") token: String?, @Path("groupId") groupId:Int,@Query("text") text : String?): Call<APIResult>
    @GET("groups/{groupId}/posts")
    fun getPosts(@Path("groupId") groupId:Int): Call<List<Post>>

}