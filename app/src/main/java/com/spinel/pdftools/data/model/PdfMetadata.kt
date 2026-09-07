package com.spinel.pdftools.data.model

import org.json.JSONObject

enum class FileSource { CREATED, OPENED }

data class PdfMetadata(
    val uri: String,
    val displayName: String,
    val source: FileSource,
    val createdAt: Long,
    val lastOpenedAt: Long,
    val size: Long
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("uri", uri)
        json.put("displayName", displayName)
        json.put("source", source.name)
        json.put("createdAt", createdAt)
        json.put("lastOpenedAt", lastOpenedAt)
        json.put("size", size)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): PdfMetadata {
            return PdfMetadata(
                uri = json.getString("uri"),
                displayName = json.getString("displayName"),
                source = FileSource.valueOf(json.getString("source")),
                createdAt = json.getLong("createdAt"),
                lastOpenedAt = json.getLong("lastOpenedAt"),
                size = json.getLong("size")
            )
        }
    }
}
