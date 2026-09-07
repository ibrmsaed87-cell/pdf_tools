package com.spinel.pdftools.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.spinel.pdftools.data.model.FileSource
import com.spinel.pdftools.data.model.PdfMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

val Context.filesDataStore by preferencesDataStore(name = "files_metadata")

class FilesRepository(private val context: Context) {
    private val METADATA_KEY = stringPreferencesKey("pdf_metadata_list")

    val filesFlow: Flow<List<PdfMetadata>> = context.filesDataStore.data.map { preferences ->
        val jsonString = preferences[METADATA_KEY] ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<PdfMetadata>()
        for (i in 0 until jsonArray.length()) {
            list.add(PdfMetadata.fromJson(jsonArray.getJSONObject(i)))
        }
        list
    }

    suspend fun addOrUpdateFile(uri: Uri, source: FileSource) {
        val displayNameAndSize = getFileInfo(uri)
        val now = System.currentTimeMillis()
        
        context.filesDataStore.edit { preferences ->
            val jsonString = preferences[METADATA_KEY] ?: "[]"
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<PdfMetadata>()
            for (i in 0 until jsonArray.length()) {
                list.add(PdfMetadata.fromJson(jsonArray.getJSONObject(i)))
            }
            
            val existingIndex = list.indexOfFirst { it.uri == uri.toString() }
            if (existingIndex >= 0) {
                val existing = list[existingIndex]
                list[existingIndex] = existing.copy(
                    lastOpenedAt = now,
                    displayName = displayNameAndSize.first ?: existing.displayName,
                    size = displayNameAndSize.second ?: existing.size
                )
            } else {
                list.add(
                    PdfMetadata(
                        uri = uri.toString(),
                        displayName = displayNameAndSize.first ?: "Unknown Document",
                        source = source,
                        createdAt = now,
                        lastOpenedAt = now,
                        size = displayNameAndSize.second ?: 0L
                    )
                )
            }
            
            val newJsonArray = JSONArray()
            list.forEach { newJsonArray.put(it.toJson()) }
            preferences[METADATA_KEY] = newJsonArray.toString()
        }
    }

    private fun getFileInfo(uri: Uri): Pair<String?, Long?> {
        var displayName: String? = null
        var size: Long? = null
        try {
            // For file:// URIs or other schemes where query doesn't work well
            if (uri.scheme == "file") {
                val file = java.io.File(uri.path ?: "")
                return Pair(file.name, file.length())
            }

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex)
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(displayName, size)
    }
}
