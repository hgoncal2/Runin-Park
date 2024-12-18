package com.ipt.runinpark.retrofit.service

import com.ipt.runinpark.model.APIResult
import com.ipt.runinpark.model.Token
import com.ipt.runinpark.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
//Faz login,a API devolve um Token
    @POST("login")
    fun login(@Query("username") username: String?,
                 @Query("password") password: String?): Call<Token>

//Remove utilizador de um grupo
    @DELETE("groups/{groupId}/members")
    fun removeUserFromGroup(@Header("auth") token: String?,@Path("groupId") groupId:Int): Call<APIResult>
    //Devolve um utilizador
    @GET("users/{username}")
    fun getUser(@Path("username") username:String): Call<User>
    //Devolve um utilizador,dado um token
    @GET("users/auth/{token}")
    fun getUserWithToken(@Path("token") token:String): Call<User>
    //Adiciona um utilizador a um grupo
    @POST("groups/{groupId}/members")
    fun addUserToGroup(@Header("auth") token: String?,@Path("groupId") groupId:Int): Call<APIResult>
    //Atualiza dados de um utilizador
    @PUT("users/{username}")
    fun updateUser(@Header("auth") token: String?,@Body user:User,@Path("username") username:String?): Call<APIResult>
    //Regista um utilizador
@POST("register")
fun register(@Query("username") username: String?,
          @Query("password") password: String?


): Call<APIResult>
}