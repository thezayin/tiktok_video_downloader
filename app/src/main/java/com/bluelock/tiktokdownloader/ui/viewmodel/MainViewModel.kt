package com.bluelock.tiktokdownloader.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bluelock.tiktokdownloader.data.listener.Response
import com.bluelock.tiktokdownloader.data.repository.Repository
import com.bluelock.tiktokdownloader.di.model.VideoData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val apiRepository: Repository
) : ViewModel(), Response {

    private val _responseLiveData = MutableLiveData<VideoData>()
    val responseLiveData: LiveData<VideoData> = _responseLiveData


    init {
        apiRepository.setListener(this)
    }

    fun getVideoData(url: String) {
        viewModelScope.launch {
            apiRepository.getVideoData(url)
        }
    }

    override fun videoResponse(video: VideoData) {
        _responseLiveData.value = video
    }

}