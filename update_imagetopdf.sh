#!/bin/bash
sed -i 's/onClick = {/onClick = { com.spinel.pdftools.monetization.InterstitialAdManager.showInterstitialIfEligible(context as android.app.Activity) {/g' app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt
