import re

with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure imports are present
imports_to_add = """
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
"""

# replace imports (we will insert it after the first import)
content = content.replace('import androidx.compose.foundation.clickable', imports_to_add + '\nimport androidx.compose.foundation.clickable')


replacement = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val coroutineScope = rememberCoroutineScope()
    
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguage = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"
    
    val setLanguage: (String) -> Unit = { langTag ->
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
    }
    
    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = stringResource(id = R.string.nav_settings),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(id = R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsSectionTitle(title = stringResource(id = R.string.section_preferences))
        
        SettingsCard {
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_appearance),
                subtitle = when (themeMode) {
                    ThemeMode.SYSTEM -> stringResource(id = R.string.theme_system)
                    ThemeMode.LIGHT -> stringResource(id = R.string.theme_light)
                    ThemeMode.DARK -> stringResource(id = R.string.theme_dark)
                },
                icon = Icons.Filled.Palette,
                iconTint = com.spinel.pdftools.ui.theme.AccentPurple,
                onClick = { showAppearanceSheet = true },
                showDivider = true
            )
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_language),
                subtitle = when (currentLanguage) {
                    "ar" -> stringResource(id = R.string.lang_arabic)
                    "es" -> stringResource(id = R.string.lang_spanish)
                    else -> stringResource(id = R.string.lang_english)
                },
                icon = Icons.Filled.Language,
                iconTint = com.spinel.pdftools.ui.theme.AccentBlue,
                onClick = { showLanguageSheet = true },
                showDivider = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSectionTitle(title = stringResource(id = R.string.section_privacy_about))
        
        SettingsCard {
            PremiumSettingsRow(
                title = stringResource(id = R.string.privacy_policy),
                subtitle = null,
                icon = Icons.Filled.PrivacyTip,
                iconTint = com.spinel.pdftools.ui.theme.AccentTeal,
                onClick = { /* Coming soon */ },
                showDivider = true
            )
            PremiumSettingsRow(
                title = stringResource(id = R.string.setting_about),
                subtitle = null,
                icon = Icons.Filled.Info,
                iconTint = com.spinel.pdftools.ui.theme.AccentOrange,
                onClick = { /* Coming soon */ },
                showDivider = false
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
    
    if (showAppearanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAppearanceSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.setting_appearance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_system),
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.SYSTEM) }
                        showAppearanceSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_light),
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.LIGHT) }
                        showAppearanceSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.theme_dark),
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { 
                        coroutineScope.launch { themeManager.setThemeMode(ThemeMode.DARK) }
                        showAppearanceSheet = false 
                    }
                )
            }
        }
    }
    
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(id = R.string.setting_language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_english),
                    selected = currentLanguage == "en",
                    onClick = { 
                        setLanguage("en")
                        showLanguageSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_arabic),
                    selected = currentLanguage == "ar",
                    onClick = { 
                        setLanguage("ar")
                        showLanguageSheet = false 
                    }
                )
                BottomSheetRadioItem(
                    text = stringResource(id = R.string.lang_spanish),
                    selected = currentLanguage == "es",
                    onClick = { 
                        setLanguage("es")
                        showLanguageSheet = false 
                    }
                )
            }
        }
    }
}

@Composable
fun PremiumSettingsRow(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), modifier = Modifier.padding(start = 72.dp, end = 16.dp))
        }
    }
}

@Composable
fun BottomSheetRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

"""

# We need to replace everything from @Composable fun SettingsScreen to the end of the file.
start_idx = content.find('@Composable\nfun SettingsScreen()')
if start_idx != -1:
    content = content[:start_idx] + replacement
    
    with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'w', encoding='utf-8') as f:
        f.write(content)
else:
    print("Could not find start idx")
