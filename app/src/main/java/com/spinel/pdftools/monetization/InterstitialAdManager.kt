package com.spinel.pdftools.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.UserMessagingPlatform

object InterstitialAdManager {
    // TEST Interstitial ID only during development
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    var isShowingAd = false
        private set
    
    private var totalSuccessfulOperations = 0
    private var hasPendingOpportunity = false
    
    // Increment frequency counter and determine if an ad is eligible
    fun recordSuccessfulOperation() {
        totalSuccessfulOperations++
        
        if (hasPendingOpportunity) {
            Log.d("InterstitialManager", "Opportunity already pending. Not stacking. Total: $totalSuccessfulOperations")
            AdDebugInfo.setOperations(totalSuccessfulOperations, hasPendingOpportunity)
            return
        }
        
        Log.d("InterstitialManager", "Operation successful. Total ops: $totalSuccessfulOperations")
        
        // Trigger eligible opportunity after EVERY successful operation
        hasPendingOpportunity = true
        Log.d("InterstitialManager", "Interstitial opportunity now eligible/pending.")
        AdDebugInfo.setOperations(totalSuccessfulOperations, hasPendingOpportunity)
    }
    
    // Preload ad when consent allows
    fun preloadAd(context: Context) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (!consentInformation.canRequestAds()) {
            Log.d("InterstitialManager", "Consent not granted to request ads.")
            return
        }
        
        if (interstitialAd != null || isAdLoading) {
            return
        }
        
        isAdLoading = true
        AdDebugInfo.setPreloadAttempted()
        Log.d("InterstitialManager", "Preloading interstitial ad...")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("InterstitialManager", "Ad failed to load: ${adError.message}")
                    interstitialAd = null
                    isAdLoading = false
                    AdDebugInfo.setLoadFailed(adError.code, adError.message)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("InterstitialManager", "Ad loaded successfully.")
                    interstitialAd = ad
                    isAdLoading = false
                    AdDebugInfo.setLoadSuccess()
                }
            }
        )
    }
    
    // Attempts to show the ad if eligible. Never blocks.
    fun showInterstitialIfEligible(activity: Activity, onAdDismissedOrSkipped: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        if (!consentInformation.canRequestAds()) {
            AdDebugInfo.setBoundaryEvaluation("Consent blocked")
            Log.d("InterstitialManager", "Skipping ad show. Consent: ${consentInformation.canRequestAds()}, Pending: $hasPendingOpportunity")
            onAdDismissedOrSkipped()
            return
        }
        if (!hasPendingOpportunity) {
            AdDebugInfo.setBoundaryEvaluation("No pending opportunity")
            Log.d("InterstitialManager", "Skipping ad show. Consent: ${consentInformation.canRequestAds()}, Pending: $hasPendingOpportunity")
            onAdDismissedOrSkipped()
            return
        }
        
        // We reached a safe boundary and an opportunity was pending, so we consume it exactly once.
        hasPendingOpportunity = false
        AdDebugInfo.setOperations(totalSuccessfulOperations, hasPendingOpportunity)
        Log.d("InterstitialManager", "Consuming pending interstitial opportunity.")
        
        if (interstitialAd != null) {
            Log.d("InterstitialManager", "Showing interstitial ad.")
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("InterstitialManager", "Ad dismissed.")
                    isShowingAd = false
                    interstitialAd = null
                    AdDebugInfo.setBoundaryEvaluation("Dismissed")
                    // Preload next ad
                    preloadAd(activity)
                    onAdDismissedOrSkipped()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("InterstitialManager", "Ad failed to show: ${adError.message}")
                    isShowingAd = false
                    interstitialAd = null
                    AdDebugInfo.setBoundaryEvaluation("Show failed")
                    onAdDismissedOrSkipped()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("InterstitialManager", "Ad showed successfully.")
                    isShowingAd = true
                    AdDebugInfo.setBoundaryEvaluation("Ad shown")
                    interstitialAd = null // nullify to prevent double showing
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialManager", "Eligible ad not loaded. Skipping without blocking user.")
            AdDebugInfo.setBoundaryEvaluation("Ad unavailable")
            // Preload next ad so it's ready for next time
            preloadAd(activity)
            onAdDismissedOrSkipped()
        }
    }
    
    // For testing purposes
    fun resetState() {
        totalSuccessfulOperations = 0
        hasPendingOpportunity = false
        interstitialAd = null
        isAdLoading = false
    }
}
