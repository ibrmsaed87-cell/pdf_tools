
package com.spinel.pdftools.ui.settings
import com.spinel.pdftools.BuildConfig
import com.spinel.pdftools.monetization.AdDebugInfo


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.pdftools.R
import android.app.Activity
import com.spinel.pdftools.monetization.LocalConsentManager

import com.spinel.pdftools.common.util.ThemeManager
import com.spinel.pdftools.common.util.ThemeMode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateToPrivacy: () -> Unit = {}, onNavigateToAbout: () -> Unit = {}) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val consentManager = LocalConsentManager.current
    val isPrivacyOptionsRequired by consentManager?.isPrivacyOptionsRequired?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf(false)
    val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val coroutineScope = rememberCoroutineScope()
    
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguage = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else context.resources.configuration.locales.get(0).language
    
    val setLanguage: (String) -> Unit = { langTag ->
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
    }
    
    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(id = R.string.nav_settings),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (BuildConfig.DEBUG) {
            val umpCanReq by AdDebugInfo.umpCanRequestAds.collectAsStateWithLifecycle()
            val umpPrivReq by AdDebugInfo.umpPrivacyOptionsRequired.collectAsStateWithLifecycle()
            val adsInit by AdDebugInfo.mobileAdsInitialized.collectAsStateWithLifecycle()
            val interPreload by AdDebugInfo.interstitialPreloadAttempted.collectAsStateWithLifecycle()
            val interLoaded by AdDebugInfo.interstitialLoaded.collectAsStateWithLifecycle()
            val lastLoadRes by AdDebugInfo.lastLoadResult.collectAsStateWithLifecycle()
            val lastLoadErrCode by AdDebugInfo.lastLoadErrorCode.collectAsStateWithLifecycle()
            val lastLoadErrMsg by AdDebugInfo.lastLoadErrorMessage.collectAsStateWithLifecycle()
            val opsRecorded by AdDebugInfo.operationsRecorded.collectAsStateWithLifecycle()
            val hasPending by AdDebugInfo.hasPendingOpportunity.collectAsStateWithLifecycle()
            val lastEval by AdDebugInfo.lastSafeBoundaryEvaluation.collectAsStateWithLifecycle()
            
            // New Diagnostic Fields
            val consentInfoUpdateStatus by AdDebugInfo.consentInfoUpdateStatus.collectAsStateWithLifecycle()
            val consentInfoUpdateErrorCode by AdDebugInfo.consentInfoUpdateErrorCode.collectAsStateWithLifecycle()
            val consentInfoUpdateErrorMessage by AdDebugInfo.consentInfoUpdateErrorMessage.collectAsStateWithLifecycle()
            val consentFormResult by AdDebugInfo.consentFormResult.collectAsStateWithLifecycle()
            val consentFormErrorCode by AdDebugInfo.consentFormErrorCode.collectAsStateWithLifecycle()
            val consentFormErrorMessage by AdDebugInfo.consentFormErrorMessage.collectAsStateWithLifecycle()
            val consentStatus by AdDebugInfo.consentStatus.collectAsStateWithLifecycle()
            val privacyOptionsStatus by AdDebugInfo.privacyOptionsStatus.collectAsStateWithLifecycle()

            SettingsSectionTitle(title = "Ad Debug Status")
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Consent info update status: $consentInfoUpdateStatus", style = MaterialTheme.typography.bodyMedium)
                    if (consentInfoUpdateStatus == "Failed") {
                        Text(text = "Consent info update error code: $consentInfoUpdateErrorCode", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Consent info update error message: $consentInfoUpdateErrorMessage", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = "Consent form result: $consentFormResult", style = MaterialTheme.typography.bodyMedium)
                    if (consentFormResult == "Failed") {
                        Text(text = "Consent form error code: $consentFormErrorCode", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Consent form error message: $consentFormErrorMessage", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = "Current ConsentStatus: $consentStatus", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Current PrivacyOptionsReqStatus: $privacyOptionsStatus", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "UMP canRequestAds: $umpCanReq", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Privacy options required: $umpPrivReq", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Mobile Ads initialized: $adsInit", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Interstitial preload attempted: $interPreload", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Interstitial loaded: $interLoaded", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Last load result: $lastLoadRes", style = MaterialTheme.typography.bodyMedium)
                    if (lastLoadRes == "Failed") {
                        Text(text = "Last load error code: $lastLoadErrCode", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Last load error message: $lastLoadErrMsg", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(text = "Successful ops recorded: $opsRecorded", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Pending opportunity: $hasPending", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Last safe-boundary eval: $lastEval", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        SettingsSectionTitle(title = stringResource(id = R.string.section_preferences))
        
        SettingsCard {
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_appearance),
                subtitle = when (themeMode) {
                    ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                    ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                },
                icon = Icons.Filled.Palette,
                iconTint = com.spinel.pdftools.ui.theme.AccentPurple,
                onClick = { showAppearanceSheet = true },
                showDivider = true
            )
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_language),
                subtitle = when (currentLanguage) {
                    "ar" -> stringResource(id = R.string.lang_arabic)
                    "es" -> stringResource(id = R.string.lang_spanish)
                    else -> stringResource(id = R.string.lang_english)
                },
                icon = Icons.Filled.Language,
                iconTint = com.spinel.pdftools.ui.theme.AccentBlue,
                onClick = { showLanguageSheet = true },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionTitle(title = stringResource(id = R.string.section_privacy_about))
        
        SettingsCard {
            if (isPrivacyOptionsRequired) {
                PremiumSettingsRow(
                    title = stringResource(id = R.string.setting_privacy_options),
                    subtitle = null,
                    icon = Icons.Filled.PrivacyTip,
                    iconTint = com.spinel.pdftools.ui.theme.AccentBlue,
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null) {
                            consentManager?.showPrivacyOptionsForm(activity)
                        }
                    },
                    showDivider = true
                )
            }
            PremiumSettingsRow(
                title = stringResource(id = R.string.privacy_policy),
                subtitle = null,
                icon = Icons.Filled.PrivacyTip,
                iconTint = com.spinel.pdftools.ui.theme.AccentTeal,
                onClick = onNavigateToPrivacy,
                showDivider = true
            )
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_about),
                subtitle = null,
                icon = Icons.Filled.Info,
                iconTint = com.spinel.pdftools.ui.theme.AccentOrange,
                onClick = onNavigateToAbout,
                showDivider = false
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
    
    if (showAppearanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppearanceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.setting_appearance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.SYSTEM) }
                        showAppearanceSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.LIGHT) }
                        showAppearanceSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.DARK) }
                        showAppearanceSheet = false 
                    }
                )
            }
        }
    }
    
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.setting_language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_english),
                    selected = currentLanguage == "en",
                    onClick = { 
                        setLanguage("en")
                        showLanguageSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_arabic),
                    selected = currentLanguage == "ar",
                    onClick = { 
                        setLanguage("ar")
                        showLanguageSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_spanish),
                    selected = currentLanguage == "es",
                    onClick = { 
                        setLanguage("es")
                        showLanguageSheet = false 
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumSettingsRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 72.dp, end = 16.dp))
        }
    }
}

@Composable
fun BottomSheetRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 12.dp)
    )
}
