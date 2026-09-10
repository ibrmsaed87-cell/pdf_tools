#!/bin/bash
sed -i '1i\
import com.spinel.pdftools.BuildConfig\
import com.spinel.pdftools.monetization.AdDebugInfo\
' ./app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt
