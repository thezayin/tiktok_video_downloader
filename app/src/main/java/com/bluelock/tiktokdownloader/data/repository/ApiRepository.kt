package com.bluelock.tiktokdownloader.data.repository

import com.bluelock.tiktokdownloader.data.listener.ApiResponse

interface ApiRepository {

    //Call
    suspend fun getVideoData(videoUrl: String)

    //Response
     fun setApiListener(apiResponse: ApiResponse)

}