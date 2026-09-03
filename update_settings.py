import re

with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Imports
content = content.replace('import androidx.compose.ui.platform.LocalContext', 'import androidx.compose.ui.platform.LocalContext\nimport androidx.appcompat.app.AppCompatDelegate\nimport androidx.core.os.LocaleListCompat')

# Usage in SettingsScreen
content = content.replace('LanguageSelectionPlaceholder()', 'LanguageSelection()')

# Replacement components
replacement = """
@Composable
fun LanguageSelection() {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguage = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"
    
    val setLanguage: (String) -> Unit = { langTag ->
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
    }

    Column {
        LanguageRadioItem(text = "English", selected = currentLanguage == "en", onClick = { setLanguage("en") }, showDivider = true)
        LanguageRadioItem(text = "العربية", selected = currentLanguage == "ar", onClick = { setLanguage("ar") }, showDivider = true)
        LanguageRadioItem(text = "Español", selected = currentLanguage == "es", onClick = { setLanguage("es") }, showDivider = false)
    }
}

@Composable
fun LanguageRadioItem(text: String, selected: Boolean, onClick: () -> Unit, showDivider: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
"""

content = re.sub(r'@Composable\nfun LanguageSelectionPlaceholder\(\) \{.*?(?=@Composable)', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
