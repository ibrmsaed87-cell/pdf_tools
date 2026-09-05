package com.spinel.pdftools.ui.imagetopdf

import android.net.Uri
import java.util.UUID

enum class TextAlignment { Start, Center, End }

enum class TextColor(val colorValue: Long) {
    Black(0xFF000000),
    DarkGray(0xFF444444),
    Red(0xFFD32F2F),
    Blue(0xFF1976D2),
    Green(0xFF388E3C),
    Purple(0xFF7B1FA2),
    White(0xFFFFFFFF)
}

data class TextStyleConfig(
    val alignment: TextAlignment = TextAlignment.Start,
    val fontSize: Int = 18,
    val isBold: Boolean = false,
    val color: TextColor = TextColor.Black
)

sealed class DocumentPage {
    abstract val id: String
    
    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val uri: Uri
    ) : DocumentPage()
    
    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val title: String,
        val body: String,
        val titleStyle: TextStyleConfig = TextStyleConfig(fontSize = 28, isBold = true),
        val bodyStyle: TextStyleConfig = TextStyleConfig()
    ) : DocumentPage()
}
