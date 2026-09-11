package com.spinel.pdftools.monetization

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NativeAdManager {
    // TEST Native Advanced ID only during development
    private const val AD_UNIT_ID = "ca-app-pub-9118481973136364/3564601173"

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd.asStateFlow()

    private var isAdLoading = false

    // Load ad when consent allows
    fun loadAd(context: Context) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        if (!consentInformation.canRequestAds()) {
            Log.d("NativeAdManager", "Consent not granted to request ads.")
            return
        }

        if (_nativeAd.value != null || isAdLoading) {
            return
        }

        isAdLoading = true
        Log.d("NativeAdManager", "Loading native ad...")

        val builder = AdLoader.Builder(context, AD_UNIT_ID)
            .forNativeAd { ad ->
                Log.d("NativeAdManager", "Native ad loaded successfully.")
                val oldAd = _nativeAd.value
                _nativeAd.value = ad
                oldAd?.destroy() // Safely destroy the old one if it existed
                isAdLoading = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w("NativeAdManager", "Failed to load native ad: ${error.message}")
                    isAdLoading = false
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())

        val adLoader = builder.build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    // For testing/cleanup purposes
    fun destroy() {
        _nativeAd.value?.destroy()
        _nativeAd.value = null
        isAdLoading = false
    }
}
