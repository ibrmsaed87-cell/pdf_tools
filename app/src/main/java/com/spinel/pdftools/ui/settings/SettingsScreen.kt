package com.spinel.pdftools.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.pdftools.R
import com.spinel.pdftools.common.util.ThemeManager
import com.spinel.pdftools.common.util.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.nav_settings),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSectionTitle(title = stringResource(id = R.string.setting_appearance))
        
        ThemeSelection(
            currentTheme = themeMode,
            onThemeSelected = { mode ->
                coroutineScope.launch {
                    themeManager.setThemeMode(mode)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle(title = stringResource(id = R.string.setting_language))
        
        // Language selection UI placeholder
        LanguageSelectionPlaceholder()
        
        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle(title = stringResource(id = R.string.setting_privacy))
        SettingItemClickable(title = stringResource(id = R.string.setting_privacy))
        
        Spacer(modifier = Modifier.height(16.dp))
        SettingItemClickable(title = stringResource(id = R.string.setting_about))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun ThemeSelection(currentTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    ThemeRadioItem(
        text = stringResource(id = R.string.theme_system),
        selected = currentTheme == ThemeMode.SYSTEM,
        onClick = { onThemeSelected(ThemeMode.SYSTEM) }
    )
    ThemeRadioItem(
        text = stringResource(id = R.string.theme_light),
        selected = currentTheme == ThemeMode.LIGHT,
        onClick = { onThemeSelected(ThemeMode.LIGHT) }
    )
    ThemeRadioItem(
        text = stringResource(id = R.string.theme_dark),
        selected = currentTheme == ThemeMode.DARK,
        onClick = { onThemeSelected(ThemeMode.DARK) }
    )
}

@Composable
fun ThemeRadioItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LanguageSelectionPlaceholder() {
    Column {
        LanguageRadioItem(text = "English", selected = true)
        LanguageRadioItem(text = "العربية", selected = false)
        LanguageRadioItem(text = "Español", selected = false)
    }
}

@Composable
fun LanguageRadioItem(text: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingItemClickable(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
