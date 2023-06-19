package com.bluelock.tiktokdownloader.data.repository

import com.bluelock.tiktokdownloader.data.listener.Response


interface Repository {

    //Call
    suspend fun getVideoData(videoUrl: String)

    //Response
     fun setListener(apiResponse: Response)

}