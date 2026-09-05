package com.spinel.pdftools.ui.imagetopdf

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class GenerationState {
    object Idle : GenerationState()
    data class Generating(val current: Int, val total: Int) : GenerationState()
    data class Success(val outputUri: Uri) : GenerationState()
    data class Error(val message: String?) : GenerationState()
}

data class ImageToPdfState(
    val pages: List<DocumentPage> = emptyList(),
    val generationState: GenerationState = GenerationState.Idle,
    val isTextEditorVisible: Boolean = false,
    val editingTextPageId: String? = null
)

class ImageToPdfViewModel : ViewModel() {
    private val _state = MutableStateFlow(ImageToPdfState())
    val state: StateFlow<ImageToPdfState> = _state.asStateFlow()

    fun addImages(uris: List<Uri>) {
        val newPages = uris.map { DocumentPage.Image(uri = it) }
        _state.update { currentState ->
            currentState.copy(
                pages = currentState.pages + newPages
            )
        }
    }

    fun showTextEditor(pageId: String? = null) {
        _state.update { it.copy(isTextEditorVisible = true, editingTextPageId = pageId) }
    }

    fun hideTextEditor() {
        _state.update { it.copy(isTextEditorVisible = false, editingTextPageId = null) }
    }

    fun saveTextPage(title: String, body: String, titleStyle: TextStyleConfig, bodyStyle: TextStyleConfig) {
        _state.update { currentState ->
            if (currentState.editingTextPageId != null) {
                val updatedPages = currentState.pages.map {
                    if (it.id == currentState.editingTextPageId && it is DocumentPage.Text) {
                        it.copy(title = title, body = body, titleStyle = titleStyle, bodyStyle = bodyStyle)
                    } else it
                }
                currentState.copy(pages = updatedPages, isTextEditorVisible = false, editingTextPageId = null)
            } else {
                val newPage = DocumentPage.Text(title = title, body = body, titleStyle = titleStyle, bodyStyle = bodyStyle)
                currentState.copy(pages = currentState.pages + newPage, isTextEditorVisible = false, editingTextPageId = null)
            }
        }
    }

    fun removePage(id: String) {
        _state.update { currentState ->
            currentState.copy(
                pages = currentState.pages.filter { it.id != id }
            )
        }
    }

    fun reorderPages(fromIndex: Int, toIndex: Int) {
        _state.update { currentState ->
            val pgs = currentState.pages.toMutableList()
            if (fromIndex in pgs.indices && toIndex in pgs.indices) {
                val item = pgs.removeAt(fromIndex)
                pgs.add(toIndex, item)
                currentState.copy(pages = pgs)
            } else {
                currentState
            }
        }
    }

    fun resetState() {
        _state.update { 
            ImageToPdfState()
        }
    }
    
    fun resetGenerationState() {
        _state.update {
            it.copy(generationState = GenerationState.Idle)
        }
    }

    fun generatePdf(context: Context, outputUri: Uri) {
        val currentPages = _state.value.pages
        if (currentPages.isEmpty()) {
            _state.update { it.copy(generationState = GenerationState.Error("error_no_images")) }
            return
        }

        _state.update { it.copy(generationState = GenerationState.Generating(0, currentPages.size)) }

        viewModelScope.launch {
            val result = PdfGenerator.generatePdf(
                context = context,
                pages = currentPages,
                outputUri = outputUri,
                onProgress = { current, total ->
                    _state.update { it.copy(generationState = GenerationState.Generating(current, total)) }
                }
            )

            if (result.isSuccess) {
                _state.update { it.copy(generationState = GenerationState.Success(outputUri)) }
            } else {
                _state.update { it.copy(generationState = GenerationState.Error("error_generic")) }
            }
        }
    }
}
