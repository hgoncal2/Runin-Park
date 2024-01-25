package com.example.myapplication.retrofit.service

import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Group
import com.example.myapplication.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupService {

    @GET("groups")
    fun getGroups(): Call<List<Group>>
    @GET("groups/{name}")
    fun getGroup(@Path("name") username:String): Call<Group>

    @GET("users/{userId}/groups")
    fun getUserGroups(@Path("userId") userId:Int): Call<List<Group>>

    @GET("groups/{groupId}/members")
    fun getGroupMembers(@Path("groupId") groupId:Int): Call<List<User>>


    @POST("groups")
    fun createGroup(@Header("auth") token: String?,@Query("name") name:String,@Query("city") city:String) : Call<APIResult>

}