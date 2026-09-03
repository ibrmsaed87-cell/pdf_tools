package com.spinel.pdftools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.pdftools.common.util.ThemeManager
import com.spinel.pdftools.common.util.ThemeMode
import com.spinel.pdftools.ui.navigation.AppNavigation
import com.spinel.pdftools.ui.theme.Theme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val themeManager = ThemeManager(this)
    
    setContent {
      val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
      
      val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }

      Theme(darkTheme = isDarkTheme) {
        AppNavigation()
      }
    }
  }
}

