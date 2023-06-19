package com.bluelock.tiktokdownloader.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface API {

    @GET("api/")
     suspend fun getDownloadUrls(@Query("url") videoUrl: String): Response<JsonObject>
}