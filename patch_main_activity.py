import re

main_path = 'app/src/main/java/com/spinel/pdftools/MainActivity.kt'
with open(main_path, 'r', encoding='utf-8') as f:
    main_code = f.read()

imports = """
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.spinel.pdftools.monetization.ConsentManager
import com.spinel.pdftools.monetization.LocalConsentManager
"""
main_code = main_code.replace('import com.spinel.pdftools.ui.theme.Theme', 'import com.spinel.pdftools.ui.theme.Theme\n' + imports)

# Inside setContent
old_set_content = """        setContent {
      val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
      
      val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }
      Theme(darkTheme = isDarkTheme) {
        AppNavigation()
      }
    }"""

new_set_content = """        setContent {
      val consentManager = remember { ConsentManager(this@MainActivity) }
      androidx.compose.runtime.LaunchedEffect(Unit) {
          consentManager.gatherConsent(this@MainActivity) { }
      }
      
      val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
      
      val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
      }
      
      CompositionLocalProvider(LocalConsentManager provides consentManager) {
          Theme(darkTheme = isDarkTheme) {
            AppNavigation()
          }
      }
    }"""

main_code = main_code.replace(old_set_content, new_set_content)

with open(main_path, 'w', encoding='utf-8') as f:
    f.write(main_code)
