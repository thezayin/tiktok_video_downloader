package com.bluelock.tiktokdownloader.ui.download

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bluelock.tiktokdownloader.adapter.MyAdapter
import com.bluelock.tiktokdownloader.databinding.FragmentDownloadedBinding
import com.bluelock.tiktokdownloader.interfaces.ItemClickListener
import com.bluelock.tiktokdownloader.remote.RemoteConfig
import com.bluelock.tiktokdownloader.ui.base.BaseFragment
import com.bluelock.tiktokdownloader.util.isConnected
import com.bluelock.tiktokdownloader.util.rootFile
import com.example.ads.GoogleManager
import com.example.ads.databinding.MediumNativeAdLayoutBinding
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class DownloadedFragment : BaseFragment<FragmentDownloadedBinding>(), ItemClickListener {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDownloadedBinding =
        FragmentDownloadedBinding::inflate

    @Inject
    lateinit var googleManager: GoogleManager

    private var nativeAd: NativeAd? = null

    @Inject
    @GoogleAnalytics
    lateinit var analytics: Analytics

    @Inject
    lateinit var remoteConfig: RemoteConfig

    private var fileList: ArrayList<File> = ArrayList()
    private lateinit var myAdapter: MyAdapter

    override fun onCreatedView() {
        initRecyclerView()
        lifecycleScope.launch(Dispatchers.IO) { refreshFiles() }
        showRecursiveAds()
        initView()
    }

    private fun initView() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun initRecyclerView() {

        myAdapter = MyAdapter(fileList, this)

        binding.downloaded.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(
                    requireActivity(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = myAdapter
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun refreshFiles() {
        fileList.clear()
        rootFile.listFiles()?.let { f ->
            f.forEach {
                binding.empty.visibility = View.GONE
                fileList.add(it)
            }
        }
        withContext(Dispatchers.Main) {
            myAdapter.notifyDataSetChanged()
        }

    }


    override fun onItemClicked(file: File) {
        showRewardedAd {}
        val uri =
            FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().applicationContext.packageName + ".provider",
                file
            )

        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, requireActivity().contentResolver.getType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(this)
        }

    }

    private fun showNativeAd() {
        if (remoteConfig.showDropDownAd) {
            nativeAd = googleManager.createNativeAdSmall()
            nativeAd?.let {
                val nativeAdLayoutBinding = NativeAdBannerLayoutBinding.inflate(layoutInflater)
                nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
                binding.nativeView.removeAllViews()
                binding.nativeView.addView(nativeAdLayoutBinding.root)
                binding.nativeView.visibility = View.VISIBLE
            }
        }
    }

    private fun showDropDown() {
        val nativeAdCheck = googleManager.createNativeFull()
        nativeAdCheck?.let {
            binding.apply {
                dropLayout.bringToFront()
                nativeViewDrop.bringToFront()
            }
            val nativeAdLayoutBinding = MediumNativeAdLayoutBinding.inflate(layoutInflater)
            nativeAdLayoutBinding.nativeAdView.loadNativeAd(ad = it)
            binding.nativeViewDrop.removeAllViews()
            binding.nativeViewDrop.addView(nativeAdLayoutBinding.root)
            binding.nativeViewDrop.visibility = View.VISIBLE
            binding.dropLayout.visibility = View.VISIBLE

            binding.btnDropDown.setOnClickListener {
                binding.dropLayout.visibility = View.GONE
            }
            binding.btnDropUp.visibility = View.INVISIBLE

        }
    }

    private fun showInterstitialAd(callback: () -> Unit) {
        if (remoteConfig.showDropDownAd) {
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
                ad.show(requireActivity())
            }
        } else {
            callback.invoke()
        }
    }

    private fun showRecursiveAds() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (this.isActive) {
                    showNativeAd()
                    if (remoteConfig.nativeAd) {
                        showNativeAd()
                        showDropDown()
                        showInterstitialAd { }
                    }
                    delay(15000L)
                }
            }
        }
    }

    private fun showRewardedAd(callback: () -> Unit) {
        if (remoteConfig.showDropDownAd) {

            if (!requireActivity().isConnected()) {
                callback.invoke()
                return
            }
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
                }

                ad.show(requireActivity()) {
                    callback.invoke()
                }
            }
        } else {
            callback.invoke()
        }
    }

    override fun onDestroyedView() {
        showInterstitialAd { }
    }

}