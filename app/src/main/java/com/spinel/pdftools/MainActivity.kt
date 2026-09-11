package com.spinel.pdftools

import android.os.Bundle
import android.content.ActivityNotFoundException
import android.net.Uri
import android.content.Intent
import com.spinel.pdftools.BuildConfig
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import android.widget.Toast
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

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
import com.spinel.pdftools.monetization.AppOpenAdManager


class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handleNotificationIntent(intent)
    PDFBoxResourceLoader.init(applicationContext)
    enableEdgeToEdge()
    
    
    createNotificationChannel()
    
    FirebaseMessaging.getInstance().subscribeToTopic("docvra_all")
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("MainActivity", "FCM topic docvra_all subscription failed", task.exception)
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this@MainActivity, "FCM topic subscription FAILED", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.d("MainActivity", "FCM topic docvra_all subscription succeeded")
                if (BuildConfig.DEBUG) {
                    Toast.makeText(this@MainActivity, "FCM topic subscribed: docvra_all", Toast.LENGTH_SHORT).show()
                }
            }
        }
    
    AppOpenAdManager.register(application)

    
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


  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleNotificationIntent(intent)
  }

  private fun handleNotificationIntent(intent: Intent?) {
    if (intent == null || intent.extras == null) return
    
    val url = intent.extras?.getString("play_url") 
        ?: intent.extras?.getString("link") 
        ?: intent.extras?.getString("url")
        
    if (!url.isNullOrEmpty() && (url.startsWith("https://play.google.com/") || url.startsWith("market://details?id="))) {
        // Prevent repeated handling
        intent.removeExtra("play_url")
        intent.removeExtra("link")
        intent.removeExtra("url")
        
        try {
            val uri = Uri.parse(url)
            val viewIntent = Intent(Intent.ACTION_VIEW, uri)
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AppOpenAdManager.suppressNextAppOpen()
            startActivity(viewIntent)
        } catch (e: ActivityNotFoundException) {
            Log.e("MainActivity", "Activity not found to handle URL: $url", e)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to parse or open URL: $url", e)
        }
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "general_notifications"
        val name = getString(R.string.notification_channel_name)
        val descriptionText = getString(R.string.notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
  }
}
