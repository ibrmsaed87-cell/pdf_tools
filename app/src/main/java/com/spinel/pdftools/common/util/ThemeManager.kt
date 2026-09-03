package com.spinel.pdftools.common.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pdf_tools_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

class ThemeManager(private val context: Context) {

    companion object {
        private val THEME_KEY = intPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val index = preferences[THEME_KEY] ?: ThemeMode.SYSTEM.ordinal
            ThemeMode.entries.getOrElse(index) { ThemeMode.SYSTEM }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = mode.ordinal
        }
    }
}
