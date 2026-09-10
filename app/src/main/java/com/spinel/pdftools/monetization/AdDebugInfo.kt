package com.spinel.pdftools.monetization

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdDebugInfo {
    private val _umpCanRequestAds = MutableStateFlow(false)
    val umpCanRequestAds: StateFlow<Boolean> = _umpCanRequestAds.asStateFlow()

    private val _umpPrivacyOptionsRequired = MutableStateFlow(false)
    val umpPrivacyOptionsRequired: StateFlow<Boolean> = _umpPrivacyOptionsRequired.asStateFlow()

    private val _mobileAdsInitialized = MutableStateFlow(false)
    val mobileAdsInitialized: StateFlow<Boolean> = _mobileAdsInitialized.asStateFlow()

    private val _interstitialPreloadAttempted = MutableStateFlow(false)
    val interstitialPreloadAttempted: StateFlow<Boolean> = _interstitialPreloadAttempted.asStateFlow()

    private val _interstitialLoaded = MutableStateFlow(false)
    val interstitialLoaded: StateFlow<Boolean> = _interstitialLoaded.asStateFlow()

    private val _lastLoadResult = MutableStateFlow("None")
    val lastLoadResult: StateFlow<String> = _lastLoadResult.asStateFlow()

    private val _lastLoadErrorCode = MutableStateFlow<Int?>(null)
    val lastLoadErrorCode: StateFlow<Int?> = _lastLoadErrorCode.asStateFlow()

    private val _lastLoadErrorMessage = MutableStateFlow<String?>("None")
    val lastLoadErrorMessage: StateFlow<String?> = _lastLoadErrorMessage.asStateFlow()

    private val _operationsRecorded = MutableStateFlow(0)
    val operationsRecorded: StateFlow<Int> = _operationsRecorded.asStateFlow()

    private val _hasPendingOpportunity = MutableStateFlow(false)
    val hasPendingOpportunity: StateFlow<Boolean> = _hasPendingOpportunity.asStateFlow()

    private val _lastSafeBoundaryEvaluation = MutableStateFlow("None")
    val lastSafeBoundaryEvaluation: StateFlow<String> = _lastSafeBoundaryEvaluation.asStateFlow()

    // --- New UMP Diagnostic Fields ---
    private val _consentInfoUpdateStatus = MutableStateFlow("Not attempted")
    val consentInfoUpdateStatus: StateFlow<String> = _consentInfoUpdateStatus.asStateFlow()

    private val _consentInfoUpdateErrorCode = MutableStateFlow<String>("None")
    val consentInfoUpdateErrorCode: StateFlow<String> = _consentInfoUpdateErrorCode.asStateFlow()

    private val _consentInfoUpdateErrorMessage = MutableStateFlow<String>("None")
    val consentInfoUpdateErrorMessage: StateFlow<String> = _consentInfoUpdateErrorMessage.asStateFlow()

    private val _consentFormResult = MutableStateFlow("Not attempted")
    val consentFormResult: StateFlow<String> = _consentFormResult.asStateFlow()

    private val _consentFormErrorCode = MutableStateFlow<String>("None")
    val consentFormErrorCode: StateFlow<String> = _consentFormErrorCode.asStateFlow()

    private val _consentFormErrorMessage = MutableStateFlow<String>("None")
    val consentFormErrorMessage: StateFlow<String> = _consentFormErrorMessage.asStateFlow()

    private val _consentStatus = MutableStateFlow("Unknown")
    val consentStatus: StateFlow<String> = _consentStatus.asStateFlow()

    private val _privacyOptionsStatus = MutableStateFlow("Unknown")
    val privacyOptionsStatus: StateFlow<String> = _privacyOptionsStatus.asStateFlow()

    fun updateUmpStatus(
        canRequest: Boolean,
        privacyOptionsRequired: Boolean,
        cStatus: String = "Unknown",
        pStatus: String = "Unknown"
    ) {
        _umpCanRequestAds.value = canRequest
        _umpPrivacyOptionsRequired.value = privacyOptionsRequired
        if (cStatus != "Unknown") _consentStatus.value = cStatus
        if (pStatus != "Unknown") _privacyOptionsStatus.value = pStatus
    }

    fun setConsentInfoUpdateInProgress() {
        _consentInfoUpdateStatus.value = "In progress"
    }

    fun setConsentInfoUpdateSuccess() {
        _consentInfoUpdateStatus.value = "Success"
        _consentInfoUpdateErrorCode.value = "None"
        _consentInfoUpdateErrorMessage.value = "None"
    }

    fun setConsentInfoUpdateFailure(code: Int, message: String) {
        _consentInfoUpdateStatus.value = "Failed"
        _consentInfoUpdateErrorCode.value = code.toString()
        _consentInfoUpdateErrorMessage.value = message
    }

    fun setConsentFormCompleted() {
        _consentFormResult.value = "Completed"
        _consentFormErrorCode.value = "None"
        _consentFormErrorMessage.value = "None"
    }
    
    fun setConsentFormNotRequired() {
        _consentFormResult.value = "Not required"
        _consentFormErrorCode.value = "None"
        _consentFormErrorMessage.value = "None"
    }

    fun setConsentFormFailure(code: Int, message: String) {
        _consentFormResult.value = "Failed"
        _consentFormErrorCode.value = code.toString()
        _consentFormErrorMessage.value = message
    }

    fun setInitialized() { _mobileAdsInitialized.value = true }
    fun setPreloadAttempted() { _interstitialPreloadAttempted.value = true }
    fun setLoadSuccess() {
        _interstitialLoaded.value = true
        _lastLoadResult.value = "Loaded"
        _lastLoadErrorCode.value = null
        _lastLoadErrorMessage.value = null
    }
    fun setLoadFailed(code: Int, message: String) {
        _interstitialLoaded.value = false
        _lastLoadResult.value = "Failed"
        _lastLoadErrorCode.value = code
        _lastLoadErrorMessage.value = message
    }
    fun setOperations(count: Int, pending: Boolean) {
        _operationsRecorded.value = count
        _hasPendingOpportunity.value = pending
    }
    fun setBoundaryEvaluation(eval: String) {
        _lastSafeBoundaryEvaluation.value = eval
    }
}
