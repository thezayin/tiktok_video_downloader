package com.example.ads.newStrategy;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class GoogleInterstitialReward {

    private final int totalLevels = 4;
    private ArrayList<ArrayList<Object>> adUnits;

    private final String adUnitId = "\tca-app-pub-3940256099942544/5354046379";

    public GoogleInterstitialReward(Context context) {
        instantiateRewardList();
        loadInitialInterstitialsReward(context);
    }

    private void instantiateRewardList() {
        adUnits = new ArrayList<>();

        adUnits.add(0, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<RewardedInterstitialAd>())));
        adUnits.add(1, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<RewardedInterstitialAd>())));
        adUnits.add(2, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<RewardedInterstitialAd>())));
        adUnits.add(3, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<RewardedInterstitialAd>())));
        adUnits.add(4, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<RewardedInterstitialAd>())));
    }

    public void loadInitialInterstitialsReward(Context context) {
        InterstitialRewardAdLoad(context, totalLevels);
    }


    public RewardedInterstitialAd getMediumAd(Context activity) {
        return getInterstitialRewardAd(activity, 1);
    }

    public RewardedInterstitialAd getHighFloorAd(Context activity) {
        return getInterstitialRewardAd(activity, 2);
    }

    public RewardedInterstitialAd getDefaultAd(Context activity) {
        Log.d("jeje_def","defaultinterstial");
        return getInterstitialRewardAd(activity, 0);
    }


    public RewardedInterstitialAd getInterstitialRewardAd(Context activity, int maxLevel) {
        for (int i = totalLevels; i >= 0; i--) {

            if (maxLevel > i) {
                break;
            }

            ArrayList<Object> list = adUnits.get(i);
            String adunitid = (String) list.get(0);
            Stack<RewardedInterstitialAd> stack = (Stack<RewardedInterstitialAd>) list.get(1);

            InterstitialAdLoadSpecific(activity, adunitid, stack);

            if (stack != null && !stack.isEmpty()) {
                return stack.pop();
            }
        }

        return null;
    }

    public void InterstitialRewardAdLoad(Context activity, int level) {

        if (level < 0) {
            return;
        }

        if (adUnits.size() < level) {
            return;
        }

        ArrayList<Object> list = adUnits.get(level);
        String adunitid = (String) list.get(0);
        Stack<RewardedInterstitialAd> stack = (Stack<RewardedInterstitialAd>) list.get(1);

        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(activity, adunitid, adRequest, new RewardedInterstitialAdLoadCallback() {

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                InterstitialRewardAdLoad(activity, level - 1);
            }

            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                stack.push(interstitialAd);
            }
        });
    }

    public void InterstitialAdLoadSpecific(Context activity, String adUnitId, Stack<RewardedInterstitialAd> stack) {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(activity, adUnitId, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                stack.push(interstitialAd);
            }
        });
    }
}

