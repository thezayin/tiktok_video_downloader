package com.bluelock.tiktokdownloader.utils

import android.app.Application
import com.example.ads.GoogleManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UserApplication : Application() {

    @Inject
    lateinit var googleManager: GoogleManager
    override fun onCreate() {
        super.onCreate()
        googleManager.init()
    }
}