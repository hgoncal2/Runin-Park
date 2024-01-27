package com.example.myapplication.retrofit

import com.example.myapplication.retrofit.service.GroupService
import com.example.myapplication.retrofit.service.PhotoService
import com.example.myapplication.retrofit.service.PostService
import com.example.myapplication.retrofit.service.RunService
import com.example.myapplication.retrofit.service.UserService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInit {
    //Serializar de json
    private val gson: Gson = GsonBuilder().serializeNulls().setLenient().setDateFormat("dd-MM-yyyy HH:mm:ss").create()
    //Endereço IP público da API
    private val host = "http://16.170.180.240:5000"
    //Constrói o objeto retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    fun userService() = retrofit.create(UserService::class.java)
    fun groupService() = retrofit.create(GroupService::class.java)
    fun photoService() = retrofit.create(PhotoService::class.java)
    fun postService() = retrofit.create(PostService::class.java)
    fun runService() = retrofit.create(RunService::class.java)

}