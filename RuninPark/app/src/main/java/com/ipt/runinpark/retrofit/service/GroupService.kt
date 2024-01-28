package com.ipt.runinpark.retrofit.service

import com.ipt.runinpark.model.APIResult
import com.ipt.runinpark.model.Group
import com.ipt.runinpark.model.User
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupService {
    //Devolve todos os grupos
    @GET("groups")
    fun getGroups(): Call<List<Group>>
    //Devolve grupo por nome
    @GET("groups/{name}")
    fun getGroup(@Path("name") username:String): Call<Group>
//Devolve grupos de um utilizador(userId)
    @GET("users/{userId}/groups")
    fun getUserGroups(@Path("userId") userId:Int): Call<List<Group>>
//Devolve membros de um grupo
    @GET("groups/{groupId}/members")
    fun getGroupMembers(@Path("groupId") groupId:Int): Call<List<User>>
//Remove membro de um grupo(apenas o owner do grupo pode remover membros)
    @DELETE("groups/{groupId}/members/{userId}")
    fun removeUserFromGroup(@Header("auth") token: String?,@Path("groupId") groupId:Int,@Path("userId") userId:Int) : Call<APIResult>
//cria um grupo
    @POST("groups")
    fun createGroup(@Header("auth") token: String?,@Query("name") name:String,@Query("city") city:String) : Call<APIResult>

}