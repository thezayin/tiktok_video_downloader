package com.example.ads.newStrategy

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import java.util.Stack

class GoogleAppOpen(context: Context?) {
    private val totalLevels = 4
    private var adUnits: ArrayList<ArrayList<Any>>? = null
    private val appOpen5 = "ca-app-pub-9507635869843997/1003300021"
    private val appOpen4 = "ca-app-pub-9507635869843997/2316381696"
    private val appOpenHigh = "ca-app-pub-9507635869843997/6962149812"
    private val appOpenMed = "ca-app-pub-9507635869843997/3019035377"
    private val appOpenAll = "ca-app-pub-9507635869843997/4631143758"

    init {
        instantiateList()
        loadAppopenStart(context)
    }

    private fun instantiateList() {
        adUnits = ArrayList()
//                val testId = "ca-app-pub-3940256099942544/3419835294"
//
//        adUnits!!.add(0, ArrayList(listOf(testId, Stack<AppOpenAd>())))
//        adUnits!!.add(1, ArrayList(listOf(testId, Stack<AppOpenAd>())))
//        adUnits!!.add(2, ArrayList(listOf(testId, Stack<AppOpenAd>())))
//        adUnits!!.add(3, ArrayList(listOf(testId, Stack<AppOpenAd>())))
//        adUnits!!.add(4, ArrayList(listOf(testId, Stack<AppOpenAd>())))
        adUnits!!.add(0, ArrayList(listOf(appOpen5, Stack<AppOpenAd>())))
        adUnits!!.add(1, ArrayList(listOf(appOpen4, Stack<AppOpenAd>())))
        adUnits!!.add(2, ArrayList(listOf(appOpenHigh, Stack<AppOpenAd>())))
        adUnits!!.add(3, ArrayList(listOf(appOpenMed, Stack<AppOpenAd>())))
        adUnits!!.add(4, ArrayList(listOf(appOpenAll, Stack<AppOpenAd>())))
    }

    fun loadAppopenStart(context: Context?) {
        AppOpenAdLoad(context, totalLevels)
    }

    fun getAd(activity: Context?): AppOpenAd? {
        for (i in totalLevels downTo 0) {
            val list = adUnits!![i]
            val adunitid = list[0] as String
            val stack = list[1] as Stack<AppOpenAd>
            AppOpenLoadSpecific(activity, adunitid, stack)
            if (stack != null && !stack.isEmpty()) {
                return stack.pop()
            }
        }
        return null
    }

    fun AppOpenAdLoad(activity: Context?, level: Int) {
        if (level < 0) {
            return
        }
        if (adUnits!!.size < level) {
            return
        }
        val list = adUnits!![level]
        val adunitid = list[0] as String
        val stack = list[1] as Stack<AppOpenAd>
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            adunitid,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    stack.push(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AppOpenAdLoad(activity, level - 1)
                }
            })
    }

    fun AppOpenLoadSpecific(activity: Context?, adUnitId: String?, stack: Stack<AppOpenAd>?) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            activity,
            adUnitId,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    stack!!.push(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
            })
    }
}