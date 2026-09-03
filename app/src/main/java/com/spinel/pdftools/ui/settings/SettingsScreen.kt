package com.spinel.pdftools.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSectionTitle(title = stringResource(id = R.string.setting_appearance))
        
        SettingsCard {
            ThemeSelection(
                currentTheme = themeMode,
                onThemeSelected = { mode ->
                    coroutineScope.launch {
                        themeManager.setThemeMode(mode)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle(title = stringResource(id = R.string.setting_language))
        
        SettingsCard {
            LanguageSelectionPlaceholder()
        }

        Spacer(modifier = Modifier.height(24.dp))
        SettingsSectionTitle(title = stringResource(id = R.string.setting_privacy))
        
        SettingsCard {
            SettingItemClickable(title = stringResource(id = R.string.setting_privacy), showDivider = true)
            SettingItemClickable(title = stringResource(id = R.string.setting_about), showDivider = false)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 12.dp)
    )
}

@Composable
fun ThemeSelection(currentTheme: ThemeMode, onThemeSelected: (ThemeMode) -> Unit) {
    Column {
        ThemeRadioItem(
            text = stringResource(id = R.string.theme_system),
            selected = currentTheme == ThemeMode.SYSTEM,
            onClick = { onThemeSelected(ThemeMode.SYSTEM) },
            showDivider = true
        )
        ThemeRadioItem(
            text = stringResource(id = R.string.theme_light),
            selected = currentTheme == ThemeMode.LIGHT,
            onClick = { onThemeSelected(ThemeMode.LIGHT) },
            showDivider = true
        )
        ThemeRadioItem(
            text = stringResource(id = R.string.theme_dark),
            selected = currentTheme == ThemeMode.DARK,
            onClick = { onThemeSelected(ThemeMode.DARK) },
            showDivider = false
        )
    }
}

@Composable
fun ThemeRadioItem(text: String, selected: Boolean, onClick: () -> Unit, showDivider: Boolean) {
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
                color = MaterialTheme.colorScheme.onSurface,
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

@Composable
fun LanguageSelectionPlaceholder() {
    Column {
        LanguageRadioItem(text = "English", selected = true, showDivider = true)
        LanguageRadioItem(text = "العربية", selected = false, showDivider = true)
        LanguageRadioItem(text = "Español", selected = false, showDivider = false)
    }
}

@Composable
fun LanguageRadioItem(text: String, selected: Boolean, showDivider: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle language change later */ }
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

@Composable
fun SettingItemClickable(title: String, showDivider: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
