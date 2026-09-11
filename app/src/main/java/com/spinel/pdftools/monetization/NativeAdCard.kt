package com.spinel.pdftools.monetization

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAdView
import com.spinel.pdftools.R

@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val nativeAd by NativeAdManager.nativeAd.collectAsState()
    val currentAd = nativeAd ?: return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val primaryColor = MaterialTheme.colorScheme.onSurface.toArgb()
        val secondaryColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
        val buttonColor = MaterialTheme.colorScheme.primary.toArgb()
        val buttonTextColor = MaterialTheme.colorScheme.onPrimary.toArgb()

        AndroidView(
            modifier = Modifier.padding(12.dp),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adView = inflater.inflate(R.layout.native_ad_layout, null) as NativeAdView

                adView.headlineView = adView.findViewById(R.id.ad_headline)
                adView.bodyView = adView.findViewById(R.id.ad_body)
                adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                adView.iconView = adView.findViewById(R.id.ad_app_icon)
                adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

                adView
            },
            update = { adView ->
                (adView.headlineView as? TextView)?.text = currentAd.headline
                (adView.headlineView as? TextView)?.setTextColor(primaryColor)
                
                if (currentAd.body == null) {
                    adView.bodyView?.visibility = View.INVISIBLE
                } else {
                    adView.bodyView?.visibility = View.VISIBLE
                    (adView.bodyView as? TextView)?.text = currentAd.body
                    (adView.bodyView as? TextView)?.setTextColor(secondaryColor)
                }

                if (currentAd.callToAction == null) {
                    adView.callToActionView?.visibility = View.INVISIBLE
                } else {
                    adView.callToActionView?.visibility = View.VISIBLE
                    (adView.callToActionView as? Button)?.text = currentAd.callToAction
                    (adView.callToActionView as? Button)?.setBackgroundColor(buttonColor)
                    (adView.callToActionView as? Button)?.setTextColor(buttonTextColor)
                }

                if (currentAd.icon == null) {
                    adView.iconView?.visibility = View.GONE
                } else {
                    (adView.iconView as? ImageView)?.setImageDrawable(currentAd.icon?.drawable)
                    adView.iconView?.visibility = View.VISIBLE
                }

                if (currentAd.advertiser == null) {
                    adView.advertiserView?.visibility = View.INVISIBLE
                } else {
                    (adView.advertiserView as? TextView)?.text = currentAd.advertiser
                    (adView.advertiserView as? TextView)?.setTextColor(secondaryColor)
                    adView.advertiserView?.visibility = View.VISIBLE
                }

                adView.setNativeAd(currentAd)
            }
        )
    }
}
