package com.bluelock.tiktokdownloader.data.repository


import com.bluelock.tiktokdownloader.data.listener.Response
import com.bluelock.tiktokdownloader.data.remote.API
import com.bluelock.tiktokdownloader.di.model.VideoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoryImpl(val api: API) : Repository {

    private lateinit var listener: Response

    override suspend fun getVideoData(videoUrl: String) {
        val response = api.getDownloadUrls(videoUrl)

        if (response.isSuccessful) {
            val body = response.body()!!
            val video = if (body.get("code").asInt == 0) {
                val url = body.get("data").asJsonObject.get("play").asString
                val id = body.get("data").asJsonObject.get("id").asString
                val title = body.get("data").asJsonObject.get("title").asString
                VideoData(title, url, id)
            } else {
                VideoData("", "", "")
            }
            withContext(Dispatchers.Main) {
                listener.videoResponse(video)
            }

        }


    }

    override fun setListener(response: Response) {
        listener = response
    }
}