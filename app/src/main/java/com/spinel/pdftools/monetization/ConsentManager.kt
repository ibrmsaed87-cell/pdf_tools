package com.spinel.pdftools.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.spinel.pdftools.BuildConfig

val LocalConsentManager = staticCompositionLocalOf<ConsentManager?> { null }

class ConsentManager(private val context: Context) {
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
    private var isMobileAdsInitializeCalled = false

    private val _isPrivacyOptionsRequired = MutableStateFlow(false)
    val isPrivacyOptionsRequired: StateFlow<Boolean> = _isPrivacyOptionsRequired.asStateFlow()

    private fun getConsentStatusString(status: Int): String {
        return when (status) {
            ConsentInformation.ConsentStatus.UNKNOWN -> "UNKNOWN"
            ConsentInformation.ConsentStatus.REQUIRED -> "REQUIRED"
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> "NOT_REQUIRED"
            ConsentInformation.ConsentStatus.OBTAINED -> "OBTAINED"
            else -> "UNKNOWN_($status)"
        }
    }

    private fun pushStatusToDebugInfo() {
        AdDebugInfo.updateUmpStatus(
            canRequest = consentInformation.canRequestAds(),
            privacyOptionsRequired = _isPrivacyOptionsRequired.value,
            cStatus = getConsentStatusString(consentInformation.consentStatus),
            pStatus = consentInformation.privacyOptionsRequirementStatus.name
        )
    }

    fun gatherConsent(activity: Activity, onConsentGatheringCompleteListener: (Boolean) -> Unit) {
        val paramsBuilder = ConsentRequestParameters.Builder()
        
        val params = paramsBuilder.build()

        AdDebugInfo.setConsentInfoUpdateInProgress()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                AdDebugInfo.setConsentInfoUpdateSuccess()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w("ConsentManager", "Consent form error: ${formError.message}")
                        AdDebugInfo.setConsentFormFailure(formError.errorCode, formError.message)
                    } else {
                        val cStatus = consentInformation.consentStatus
                        if (cStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                            AdDebugInfo.setConsentFormCompleted()
                        } else {
                            AdDebugInfo.setConsentFormNotRequired()
                        }
                    }
                    updatePrivacyOptionsState()
                    pushStatusToDebugInfo()
                    initializeMobileAdsIfAllowed()
                    onConsentGatheringCompleteListener(consentInformation.canRequestAds())
                }
            },
            { requestConsentError ->
                Log.w("ConsentManager", "Consent request error: ${requestConsentError.message}")
                AdDebugInfo.setConsentInfoUpdateFailure(requestConsentError.errorCode, requestConsentError.message)
                updatePrivacyOptionsState()
                pushStatusToDebugInfo()
                initializeMobileAdsIfAllowed()
                onConsentGatheringCompleteListener(consentInformation.canRequestAds())
            }
        )
        
        if (consentInformation.canRequestAds()) {
            initializeMobileAdsIfAllowed()
        }
        updatePrivacyOptionsState()
        pushStatusToDebugInfo()
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                Log.w("ConsentManager", "Privacy options form error: ${formError.message}")
            }
            updatePrivacyOptionsState()
            AdDebugInfo.updateUmpStatus(consentInformation.canRequestAds(), _isPrivacyOptionsRequired.value)
        }
    }

    private fun updatePrivacyOptionsState() {
        _isPrivacyOptionsRequired.value =
            consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun initializeMobileAdsIfAllowed() {
        if (isMobileAdsInitializeCalled) return
        if (consentInformation.canRequestAds()) {
            isMobileAdsInitializeCalled = true
            MobileAds.initialize(context) { initializationStatus ->
                Log.d("ConsentManager", "MobileAds initialized: $initializationStatus")
                AdDebugInfo.setInitialized()
                InterstitialAdManager.preloadAd(context)
            }
        }
    }
}
