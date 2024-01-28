package com.ipt.runinpark.retrofit.service

import com.ipt.runinpark.model.APIResult
import com.ipt.runinpark.model.Post
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostService {
//Cria um post num determinado grupo
    @POST("groups/{groupId}/posts")
    fun createPostNoImg(@Header("auth") token: String?, @Path("groupId") groupId:Int,@Query("text") text : String?): Call<APIResult>
//Cria um post num determinado grupo,com fotografia associado ao post
    @POST("groups/{groupId}/posts")
    @Multipart
    fun createPost(@Part image: MultipartBody.Part?,@Header("auth") token: String?, @Path("groupId") groupId:Int,@Query("text") text : String?): Call<APIResult>
    //Devolve posts de um grupo
    @GET("groups/{groupId}/posts")
    fun getPosts(@Path("groupId") groupId:Int): Call<List<Post>>
//Remove post de um grupo
    @DELETE("groups/{groupId}/posts/{postId}")
    fun deletePost(@Header("auth") token: String?,@Path("groupId") groupId:Int,@Path("postId") postId:Int) : Call<APIResult>
    //Devolve posts de um utilizador
    @GET("users/{userId}/posts")
    fun getUserPosts(@Path("userId") userId:Int): Call<List<Post>>

}