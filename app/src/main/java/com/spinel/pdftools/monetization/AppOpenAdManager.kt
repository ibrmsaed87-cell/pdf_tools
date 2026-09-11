package com.spinel.pdftools.monetization

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.ump.UserMessagingPlatform
import java.util.Date

object AppOpenAdManager : Application.ActivityLifecycleCallbacks {
    private const val AD_UNIT_ID = "ca-app-pub-9118481973136364/5890204587"
    private const val LOG_TAG = "AppOpenAdManager"

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
        private set
    private var loadTime: Long = 0

    // Thresholds
    private const val BACKGROUND_THRESHOLD_MS = 30_000L // 30 seconds
    private const val FRESHNESS_THRESHOLD_HOURS = 4L

    private var backgroundTime: Long = 0L
    private var currentActivity: Activity? = null
    private var suppressNextAd = false

    fun suppressNextAppOpen() {
        suppressNextAd = true
    }

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (!consentInformation.canRequestAds()) {
            return
        }

        isLoadingAd = true
        Log.d(LOG_TAG, "Loading App Open Ad.")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(LOG_TAG, "App Open Ad loaded.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    Log.d(LOG_TAG, "App Open Ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(FRESHNESS_THRESHOLD_HOURS)
    }

    private fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd) {
            Log.d(LOG_TAG, "App Open Ad is already showing.")
            return
        }

        if (InterstitialAdManager.isShowingAd) {
            Log.d(LOG_TAG, "Interstitial is showing. Skipping App Open Ad.")
            return
        }

        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "App Open Ad not available or stale.")
            loadAd(activity)
            return
        }

        Log.d(LOG_TAG, "Showing App Open Ad.")
        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "App Open Ad dismissed.")
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "App Open Ad showed successfully.")
                isShowingAd = true
            }
        }
        
        isShowingAd = true
        appOpenAd?.show(activity)
    }

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        
        val timeSinceBackground = System.currentTimeMillis() - backgroundTime
        val isMeaningfulReturn = backgroundTime == 0L || timeSinceBackground > BACKGROUND_THRESHOLD_MS
        
        if (isMeaningfulReturn) {
            Log.d(LOG_TAG, "Meaningful return or cold start detected. (Time since bg: $timeSinceBackground ms)")
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            if (consentInformation.canRequestAds()) {
                 if (suppressNextAd) {
                     Log.d(LOG_TAG, "App Open Ad suppressed for this foreground transition.")
                     suppressNextAd = false
                 } else {
                     showAdIfAvailable(activity)
                 }
            }
        } else {
            Log.d(LOG_TAG, "Quick return detected (Time since bg: $timeSinceBackground ms). Skipping App Open Ad.")
        }
    }

    override fun onActivityStopped(activity: Activity) {
        backgroundTime = System.currentTimeMillis()
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
