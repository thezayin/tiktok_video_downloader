package com.bluelock.tiktokdownloader.ui.activities

import android.Manifest
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bluelock.tiktokdownloader.R
import com.bluelock.tiktokdownloader.databinding.ActivityMainBinding
import com.bluelock.tiktokdownloader.di.model.VideoData
import com.bluelock.tiktokdownloader.remote.RemoteConfig
import com.bluelock.tiktokdownloader.ui.viewmodel.MainViewModel
import com.bluelock.tiktokdownloader.utils.ItemClickListener
import com.bluelock.tiktokdownloader.utils.State
import com.bluelock.tiktokdownloader.utils.isConnected
import com.bluelock.tiktokdownloader.utils.rootFile
import com.bluelock.tiktokdownloader.utils.saveVideo
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.ads.GoogleManager
import com.example.ads.databinding.NativeAdBannerLayoutBinding
import com.example.ads.newStrategy.types.GoogleInterstitialType
import com.example.ads.ui.binding.loadNativeAd
import com.example.analytics.dependencies.Analytics
import com.example.analytics.qualifiers.GoogleAnalytics
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ItemClickListener {
    lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var googleManager: GoogleManager

    private var nativeAd: NativeAd? = null

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: RemoteConfig


    var progressDialog: BottomSheetDialog? = null
    private lateinit var viewModel: MainViewModel
    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java]
        askForPermission()
        observe()
        getReceivedData()
        showNativeAd()


    }

    private fun askForPermission() {
        permission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


    private fun observe() {
        binding.apply {
            etLink.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().trim { it <= ' ' }.isEmpty()) {
                        ivCross.visibility = View.GONE
                        btnDownload.isEnabled = false
                    } else {
                        ivCross.visibility = View.VISIBLE
                        btnDownload.isEnabled = true
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int,
                    after: Int
                ) {
                    Log.d("jejeText", "before")
                }

                override fun afterTextChanged(s: Editable) {
                    Log.d("jejeYes", "after")
                }
            })

            ivCross.setOnClickListener {
                etLink.text = null
            }
            btnDownload.setOnClickListener {

                lifecycleScope.launch {
                    showProgressDialog()
                    delay(2000)
                    hideProgressDialog()
                    downloadUrl()
                }

            }
            btnSetting.setOnClickListener {
                showInterstitialAd {
                    val intent = Intent(this@MainActivity, SettingActivity::class.java)
                    startActivity(intent)
                }
            }

            viewModel.responseLiveData.observe(this@MainActivity) {
                if (checkIfFileExist(it)) {
                    Toast.makeText(this@MainActivity, "File Already Exist", Toast.LENGTH_SHORT)
                        .show()
                    return@observe
                }
                Toast.makeText(this@MainActivity, "Downloading..", Toast.LENGTH_SHORT).show()

                Glide.with(this@MainActivity).asFile().load(it.videoUrl)
                    .into(object : CustomTarget<File>() {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onResourceReady(
                            resource: File,
                            transition: Transition<in File>?
                        ) {

                            lifecycleScope.launch(Dispatchers.IO) {
                                val finished = async { saveVideo(resource, it, this@MainActivity) }
                                val state = finished.await()
                                if (state is State.COMPLETE) {
                                    withContext(Dispatchers.Main) {
                                        showSuccessDialog {
                                        }
                                    }


                                } else {
                                    val dialog =
                                        BottomSheetDialog(this@MainActivity, R.style.SheetDialog)
                                    dialog.setContentView(R.layout.dialog_download_failed)
                                    val btnOk = dialog.findViewById<Button>(R.id.btn_clear)
                                    val btnClose = dialog.findViewById<ImageView>(R.id.ivCross)
                                    val adView =
                                        dialog.findViewById<FrameLayout>(R.id.nativeViewAdSuccess)
                                    dialog.behavior.isDraggable = false
                                    dialog.setCanceledOnTouchOutside(false)
                                    if (showNatAd()) {
                                        nativeAd = googleManager.createNativeAdSmall()
                                        nativeAd?.let {
                                            val nativeAdLayoutBinding =
                                                NativeAdBannerLayoutBinding.inflate(layoutInflater)
                                            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                                            adView?.removeAllViews()
                                            adView?.addView(nativeAdLayoutBinding.root)
                                            adView?.visibility = View.VISIBLE
                                        }
                                    }

                                    btnOk?.setOnClickListener {
                                        showInterstitialAd {
                                            dialog.dismiss()
                                        }
                                    }
                                    btnClose?.setOnClickListener {
                                        showInterstitialAd {
                                            dialog.dismiss()
                                        }
                                    }

                                    dialog.show()
                                }

                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }
        }
    }


    private fun downloadUrl() {
        binding.apply {
            val url = binding.etLink.text.toString()
            if (url.isNotEmpty() && url.contains("tiktok") && url.length > 10) {
                lifecycleScope.launch {
                    val dialog = BottomSheetDialog(this@MainActivity, R.style.SheetDialog)
                    dialog.setContentView(R.layout.dialog_bottom_start_download)
                    val videoQualityTv = dialog.findViewById<Button>(R.id.btn_clear)
                    val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewAdDownload)
                    if (remoteConfig.nativeAd) {
                        nativeAd = googleManager.createNativeAdSmall()
                        nativeAd?.let {
                            val nativeAdLayoutBinding =
                                NativeAdBannerLayoutBinding.inflate(layoutInflater)
                            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                            adView?.removeAllViews()
                            adView?.addView(nativeAdLayoutBinding.root)
                            adView?.visibility = View.VISIBLE
                        }
                    }

                    dialog.behavior.isDraggable = false
                    dialog.setCanceledOnTouchOutside(false)
                    videoQualityTv?.setOnClickListener {
                        showInterstitialAd {
                            viewModel.getVideoData(url)
                            dialog.dismiss()
                        }
                    }
                    dialog.show()
                }

            } else {
                dialogValidLink()
            }
        }
    }

    private fun dialogValidLink() {
        val dialog = BottomSheetDialog(this@MainActivity, R.style.SheetDialog)
        dialog.setContentView(R.layout.dialog_invalid_link)
        val btnOk = dialog.findViewById<Button>(R.id.btn_clear)
        val cross = dialog.findViewById<ImageView>(R.id.ivCross)
        val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewInvalid)
        if (remoteConfig.nativeAd) {
            nativeAd = googleManager.createNativeAdSmall()
            nativeAd?.let {
                val nativeAdLayoutBinding =
                    NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                adView?.removeAllViews()
                adView?.addView(nativeAdLayoutBinding.root)
                adView?.visibility = View.VISIBLE
            }
        }

        dialog.behavior.isDraggable = false
        dialog.setCanceledOnTouchOutside(false)

        btnOk?.setOnClickListener {
            showInterstitialAd {
                dialog.dismiss()
            }
        }
        cross?.setOnClickListener {
            showInterstitialAd {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun checkIfFileExist(it: VideoData): Boolean {
        val saveFile = File(rootFile, it.id + ".mp4")
        return saveFile.exists()
    }

    private fun getReceivedData() {
        binding.apply {
            when (this@MainActivity.intent?.action) {
                Intent.ACTION_SEND -> {

                    this@MainActivity.intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                        etLink.setText(it)
                        btnDownload.performClick()
                    }

                }
            }
        }
    }

    private fun showProgressDialog() {
        println("Show")
        if (progressDialog != null) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
        progressDialog =
            BottomSheetDialog(this, R.style.SheetDialog)
        val inflater = LayoutInflater.from(this)
        val mView: View = inflater.inflate(R.layout.layout_progress_dialog, null)
        progressDialog?.setCancelable(false)
        progressDialog?.setContentView(mView)
        if (!progressDialog?.isShowing!! && !this.isFinishing) {
            progressDialog?.show()
        }
    }

    private fun hideProgressDialog() {
        println("Hide")
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }


    override fun onItemClicked(file: File) {
        val uri =
            FileProvider.getUriForFile(
                this@MainActivity,
                this@MainActivity.applicationContext.packageName + ".provider",
                file
            )

        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, this@MainActivity.contentResolver.getType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(this)
        }
    }

    private fun showRewardedAd(callback: () -> Unit, onDismissedEvent: () -> Unit) {
        if (this.isConnected()) {
            callback.invoke()
            return
        }
        if (true) {
            val ad: RewardedAd? =
                googleManager.createRewardedAd()


            if (ad == null) {
                callback.invoke()
            } else {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        super.onAdFailedToShowFullScreenContent(error)
                        callback.invoke()
                    }

                    override fun onAdDismissedFullScreenContent() {
                        onDismissedEvent.invoke()
                    }
                }

                ad.show(this) {
                    callback.invoke()
                }

            }
        } else {
            callback.invoke()
        }

    }


    private fun showSuccessDialog(unit: () -> Unit) {
        val dialog = BottomSheetDialog(this@MainActivity, R.style.SheetDialog)
        dialog.setContentView(R.layout.dialog_download_success)
        val btnOk = dialog.findViewById<Button>(R.id.btn_clear)
        val btnClose = dialog.findViewById<ImageView>(R.id.ivCross)
        val adView = dialog.findViewById<FrameLayout>(R.id.nativeViewAdSuccess)
        dialog.behavior.isDraggable = false
        dialog.setCanceledOnTouchOutside(false)
        if (showNatAd()) {
            nativeAd = googleManager.createNativeAdSmall()
            nativeAd?.let {
                val nativeAdLayoutBinding =
                    NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                adView?.removeAllViews()
                adView?.addView(nativeAdLayoutBinding.root)
                adView?.visibility = View.VISIBLE
            }
        }

        btnOk?.setOnClickListener {
            showInterstitialAd {
                dialog.dismiss()
                unit.invoke()
            }
        }
        btnClose?.setOnClickListener {
            showInterstitialAd {
                dialog.dismiss()
                unit.invoke()
            }
        }

        dialog.show()
    }

    fun showNatAd(): Boolean {
        return remoteConfig.nativeAd
    }

    private fun showInterstitialAd(callback: () -> Unit) {
        if (remoteConfig.showInterstitial) {
            val ad: InterstitialAd? =
                googleManager.createInterstitialAd(GoogleInterstitialType.MEDIUM)

            if (ad == null) {
                callback.invoke()
                return
            } else {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        callback.invoke()
                    }

                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        super.onAdFailedToShowFullScreenContent(error)
                        callback.invoke()
                    }
                }
                ad.show(this)
            }
        }
    }

    private fun showNativeAd() {
        if (remoteConfig.nativeAd) {
            nativeAd = googleManager.createNativeAdSmall()

            Log.d("jeje_ads",nativeAd.toString())
            nativeAd?.let {
                val nativeAdLayoutBinding = NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                binding.nativeView.removeAllViews()
                binding.nativeView.addView(nativeAdLayoutBinding.root)
                binding.nativeView.visibility = View.VISIBLE
            }
        }
    }

}