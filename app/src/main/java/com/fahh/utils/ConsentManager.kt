package com.fahh.utils

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    private var consentInformation: ConsentInformation? = null

    /**
     * Request consent info update and show the consent form if required.
     * Call this early in MainActivity (before loading ads).
     * Once consent is gathered (or not required), [onConsentResult] fires
     * so the caller can proceed to load ads.
     */
    fun requestConsent(
        activity: Activity,
        onConsentResult: () -> Unit
    ) {
        val params = ConsentRequestParameters.Builder().build()
        val info = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation = info

        info.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Consent info updated — show form if needed
                if (info.isConsentFormAvailable) {
                    loadAndShowForm(activity, onConsentResult)
                } else {
                    onConsentResult()
                }
            },
            {
                // Failed to get consent info — proceed anyway so ads still work
                onConsentResult()
            }
        )
    }

    /** Whether we can request ads (consent gathered or not required). */
    fun canRequestAds(context: Context): Boolean {
        val info = consentInformation
            ?: UserMessagingPlatform.getConsentInformation(context)
        return info.canRequestAds()
    }

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation?.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacyOptions(activity: Activity, onDismissed: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            onDismissed()
        }
    }

    private fun loadAndShowForm(activity: Activity, onConsentResult: () -> Unit) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
            // Form dismissed or not needed — proceed
            onConsentResult()
        }
    }
}
