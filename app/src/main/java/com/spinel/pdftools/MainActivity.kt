package com.spinel.pdftools

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.pdftools.common.util.ThemeManager
import com.spinel.pdftools.common.util.ThemeMode
import com.spinel.pdftools.ui.navigation.AppNavigation
import com.spinel.pdftools.ui.theme.Theme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.spinel.pdftools.monetization.ConsentManager
import com.spinel.pdftools.monetization.LocalConsentManager


class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    PDFBoxResourceLoader.init(applicationContext)
    enableEdgeToEdge()
    
    val themeManager = ThemeManager(this)
    val consentManager = ConsentManager(this)

    consentManager.gatherConsent(this) { consentGranted ->
        // Internally handles initialization
    }
    
    setContent {
      val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
      
      val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      Theme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalConsentManager provides consentManager) {
        AppNavigation()
        }
      }
    }
  }
}

// cache bust
