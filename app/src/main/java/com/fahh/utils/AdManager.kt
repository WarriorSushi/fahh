package com.fahh.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
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
