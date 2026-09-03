package com.spinel.pdftools.ui.imagetopdf

import android.net.Uri
import java.util.UUID

sealed class DocumentPage {
    abstract val id: String
    
    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        val uri: Uri
    ) : DocumentPage()
    
    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        val title: String,
        val body: String
    ) : DocumentPage()
}
