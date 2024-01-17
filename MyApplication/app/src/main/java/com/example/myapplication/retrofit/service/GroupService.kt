package com.example.myapplication.retrofit.service

import com.example.myapplication.model.Group
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GroupService {

    @GET("groups")
    fun getGroups(): Call<List<Group>>
    @GET("groups/{name}")
    fun getGroup(@Path("name") username:String): Call<Group>

    @GET("users/{userId}/groups")
    fun getUserGroups(@Path("userId") userId:Int): Call<List<Group>>

}