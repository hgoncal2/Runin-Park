package com.example.myapplication.retrofit.service

import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @POST("login")
    fun login(@Query("username") username: String?,
                 @Query("password") password: String?): Call<Token>
    @GET("users/{username}")
    fun getUser(@Path("username") username:String): Call<User>

@POST("register")
fun register(@Query("username") username: String?,
          @Query("password") password: String?


): Call<APIResult>
}