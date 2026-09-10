#!/bin/bash
sed -i 's/onClick = { onViewPdf(state.outputUri) }/onClick = { com.spinel.pdftools.monetization.InterstitialAdManager.showInterstitialIfEligible(context as android.app.Activity) { onViewPdf(state.outputUri) } }/g' app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt
