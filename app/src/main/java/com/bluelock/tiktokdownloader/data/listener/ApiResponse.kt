package com.bluelock.tiktokdownloader.data.listener

import com.bluelock.tiktokdownloader.model.VideoModel

interface ApiResponse {

    fun videoResponse(video: VideoModel)
}