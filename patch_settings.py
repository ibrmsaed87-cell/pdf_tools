import re

settings_path = 'app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt'
with open(settings_path, 'r', encoding='utf-8') as f:
    settings_code = f.read()

imports = """
import android.app.Activity
import com.spinel.pdftools.monetization.LocalConsentManager
"""
if 'LocalConsentManager' not in settings_code:
    settings_code = settings_code.replace('import com.spinel.pdftools.R', 'import com.spinel.pdftools.R' + imports)

# Setup variables
old_var = "val themeManager = ThemeManager(context)"
new_var = """val themeManager = ThemeManager(context)
    val consentManager = LocalConsentManager.current
    val isPrivacyOptionsRequired by consentManager?.isPrivacyOptionsRequired?.collectAsStateWithLifecycle() ?: androidx.compose.runtime.mutableStateOf(false)"""

settings_code = settings_code.replace(old_var, new_var)

# Add row under PrivacyPolicy
old_row = """            PremiumSettingsRow(
                title = stringResource(id = R.string.privacy_policy),
                subtitle = stringResource(id = R.string.privacy_policy_desc),
                icon = Icons.Filled.PrivacyTip,
                iconTint = com.spinel.pdftools.ui.theme.AccentGreen,
                onClick = onNavigateToPrivacy,
                showDivider = true
            )"""

new_row = """            PremiumSettingsRow(
                title = stringResource(id = R.string.privacy_policy),
                subtitle = stringResource(id = R.string.privacy_policy_desc),
                icon = Icons.Filled.PrivacyTip,
                iconTint = com.spinel.pdftools.ui.theme.AccentGreen,
                onClick = onNavigateToPrivacy,
                showDivider = true
            )
            if (isPrivacyOptionsRequired) {
                PremiumSettingsRow(
                    title = stringResource(id = R.string.setting_privacy_options),
                    subtitle = "",
                    icon = Icons.Filled.PrivacyTip,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { consentManager?.showPrivacyOptionsForm(context as Activity) },
                    showDivider = true
                )
            }"""

settings_code = settings_code.replace(old_row, new_row)

with open(settings_path, 'w', encoding='utf-8') as f:
    f.write(settings_code)
