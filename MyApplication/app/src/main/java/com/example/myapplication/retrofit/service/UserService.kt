package com.example.myapplication.retrofit.service

import com.example.myapplication.model.APIResult
import com.example.myapplication.model.Token
import com.example.myapplication.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @POST("login")
    fun login(@Query("username") username: String?,
                 @Query("password") password: String?): Call<Token>
    @GET("users/{username}")
    fun getUser(@Path("username") username:String): Call<User>

    @PUT("users/{username}")
    fun updateUser(@Header("auth") token: String?,@Body user:User,@Path("username") username:String?): Call<APIResult>
@POST("register")
fun register(@Query("username") username: String?,
          @Query("password") password: String?


): Call<APIResult>
}