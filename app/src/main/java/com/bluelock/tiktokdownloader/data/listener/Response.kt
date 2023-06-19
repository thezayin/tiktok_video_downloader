package com.bluelock.tiktokdownloader.data.listener

import com.bluelock.tiktokdownloader.di.model.VideoData


interface Response {

    fun videoResponse(video: VideoData)
}