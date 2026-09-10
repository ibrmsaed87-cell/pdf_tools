#!/bin/bash
sed -i 's/val themeManager = ThemeManager(this)/val themeManager = ThemeManager(this)\n    val consentManager = ConsentManager(this)\n\n    consentManager.gatherConsent(this) { consentGranted ->\n        \/\/ Internally handles initialization\n    }/g' ./app/src/main/java/com/spinel/pdftools/MainActivity.kt

sed -i 's/Theme(darkTheme = isDarkTheme) {/Theme(darkTheme = isDarkTheme) {\n        CompositionLocalProvider(LocalConsentManager provides consentManager) {/g' ./app/src/main/java/com/spinel/pdftools/MainActivity.kt

sed -i 's/AppNavigation()/AppNavigation()\n        }/g' ./app/src/main/java/com/spinel/pdftools/MainActivity.kt
