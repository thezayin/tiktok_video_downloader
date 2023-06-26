package com.bluelock.tiktokdownloader.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bluelock.tiktokdownloader.databinding.SplashActivityBinding
import com.bluelock.tiktokdownloader.remote.RemoteConfig
import com.bluelock.tiktokdownloader.utils.isConnected
import com.example.ads.GoogleManager
import com.example.analytics.dependencies.Analytics
import com.example.analytics.events.AnalyticsEvent
import com.example.analytics.qualifiers.GoogleAnalytics
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    lateinit var binding: SplashActivityBinding

    var progressStatus = 0

    @Inject
    lateinit var googleManager: GoogleManager

    @Inject
    lateinit var remoteConfig: RemoteConfig

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Handler(Looper.getMainLooper()).postDelayed({
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }, 3000) // 3000 is the delayed time in milliseconds.

        binding.apply {
            progressStatus = progressBar.progress
            lifecycleScope.launch {
                while (true) {
                    delay(400)

                    if (progressStatus < 100) {
                        progressBar.progress = progressStatus
                        progressStatus += 10

                    } else {
                        if (remoteConfig.showAppOpenAd) {
                            if (getAppOpenAd()) {
                                getAppOpenAd()
                            } else {
                                analytics.logEvent(
                                    AnalyticsEvent.AppOpenAdEvent(
                                        status = "open_app_ad_not_load"
                                    )
                                )
                                navigateToNextScreen()
                            }
                        } else {
                            navigateToNextScreen()
                        }
                        break
                    }
                }
            }
        }
    }

    fun navigateToNextScreen() {
        this.let {
            val intent = Intent(it, MainActivity::class.java)
            it.startActivity(intent)
        }
    }

    private fun getAppOpenAd(): Boolean {

        if (this.isConnected()) return false

        val ad = googleManager.createAppOpenAd() ?: return false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                navigateToNextScreen()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                navigateToNextScreen()
            }
        }
        ad.show(this)
        return true
    }
}
