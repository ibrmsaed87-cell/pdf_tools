package com.spinel.pdftools.ui.files

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.pdftools.data.model.FileSource
import com.spinel.pdftools.data.model.PdfMetadata
import com.spinel.pdftools.data.repository.FilesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FilesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FilesRepository(application)
    
    val allFiles: StateFlow<List<PdfMetadata>> = repository.filesFlow
        .map { list -> list.sortedByDescending { it.lastOpenedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onPdfCreated(uri: Uri) {
        viewModelScope.launch {
            repository.addOrUpdateFile(uri, FileSource.CREATED)
        }
    }

    fun onPdfOpenedFromPicker(uri: Uri) {
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
            repository.addOrUpdateFile(uri, FileSource.OPENED)
        }
    }
}
