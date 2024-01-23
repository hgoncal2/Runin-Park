package com.example.myapplication.retrofit.service

import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Post
import com.example.myapplication.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PostService {
    @POST("groups/{groupId}/posts")
    fun createPost(@Header("auth") token: String?, @Path("groupId") groupId:Int): Call<APIResult>
    @GET("groups/{groupId}/posts")
    fun getPosts(@Path("groupId") groupId:Int): Call<List<Post>>
    @GET("users/{username}")
    fun getUser(@Path("username") username:String): Call<User>
    @POST("groups/{groupId}/members")
    fun addUserToGroup(@Header("auth") token: String?, @Path("groupId") groupId:Int): Call<APIResult>

    @DELETE("groups/{groupId}/members")
    fun removeUserFromGroup(@Header("auth") token: String?, @Path("groupId") groupId:Int): Call<APIResult>

    @PUT("users/{username}")
    fun updateUser(@Header("auth") token: String?, @Body user: User, @Path("username") username:String?): Call<APIResult>
}