package com.spinel.pdftools.common.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationPreferenceManager(private val context: Context) {
    companion object {
        private val HAS_REQUESTED_NOTIFICATIONS = booleanPreferencesKey("has_requested_notifications")
    }

    val hasRequestedNotifications: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAS_REQUESTED_NOTIFICATIONS] ?: false
        }

    suspend fun setHasRequestedNotifications(hasRequested: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_REQUESTED_NOTIFICATIONS] = hasRequested
        }
    }
}
