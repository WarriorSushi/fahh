package com.fahh.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
    }

    fun loadRewardedAd(
        context: Context,
        adUnitId: String,
        onAdLoaded: (RewardedAd) -> Unit,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdFailed(adError)
            }

            override fun onAdLoaded(rewardedAd: RewardedAd) {
                onAdLoaded(rewardedAd)
            }
        })
    }

    fun loadInterstitialAd(
        context: Context,
        adUnitId: String,
        onAdLoaded: (InterstitialAd) -> Unit,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdFailed(adError)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                onAdLoaded(interstitialAd)
            }
        })
    }

    fun showInterstitialAd(
        activity: Activity,
        interstitialAd: InterstitialAd,
        onDismissed: () -> Unit,
        onShowFailed: () -> Unit
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                onShowFailed()
            }
        }
        interstitialAd.show(activity)
    }

    fun showRewardedAd(
        activity: Activity,
        rewardedAd: RewardedAd,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit,
        onShowFailed: () -> Unit
    ) {
        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                onShowFailed()
            }
        }

        rewardedAd.show(activity) {
            onRewardEarned()
        }
    }
}
