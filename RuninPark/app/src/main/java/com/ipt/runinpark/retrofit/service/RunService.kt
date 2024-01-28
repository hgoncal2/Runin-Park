package com.ipt.runinpark.retrofit.service

import com.ipt.runinpark.model.APIResult
import com.ipt.runinpark.model.Run
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RunService {

//Devolve todas as corridas de um grupo
    @GET("groups/{groupId}/runs")
    fun getRuns(@Path("groupId") groupId:Int): Call<List<Run>>
    //Cria corrida num grupo
    @POST("groups/{groupId}/runs")
    fun createRunNoImg(@Header("auth") token: String?, @Path("groupId") groupId:Int, @Query("distance") distance : Double?,@Query("hours") hours : Int?,@Query("minutes") minutes : Int?,@Query("seconds") seconds : Int?): Call<APIResult>
   //Cria corrida num grupo,com imagem associada
    @POST("groups/{groupId}/runs")
    @Multipart
    fun createRun(@Part image: MultipartBody.Part?, @Header("auth") token: String?, @Path("groupId") groupId:Int, @Query("distance") distance : Double?,@Query("hours") hours : Int?,@Query("minutes") minutes : Int?,@Query("seconds") seconds : Int?): Call<APIResult>


}