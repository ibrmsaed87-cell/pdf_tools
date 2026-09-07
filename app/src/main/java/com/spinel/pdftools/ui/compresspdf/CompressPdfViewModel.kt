package com.spinel.pdftools.ui.compresspdf

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.pdftools.utils.CompressionLevel
import com.spinel.pdftools.utils.PdfCompressor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

sealed class CompressState {
    object Idle : CompressState()
    data class Selected(val uri: Uri, val name: String, val size: Long) : CompressState()
    data class Compressing(val progress: Float, val message: String) : CompressState()
    data class Saving(val progress: Float) : CompressState()
    data class Success(
        val originalSize: Long,
        val compressedSize: Long,
        val savedSize: Long,
        val reductionPercent: Int,
        val uri: Uri
    ) : CompressState()
    data class NotReduced(val message: String) : CompressState()
    data class Error(val message: String) : CompressState()
}

class CompressPdfViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<CompressState>(CompressState.Idle)
    val state: StateFlow<CompressState> = _state.asStateFlow()

    private var compressedFile: File? = null
    private var originalSize: Long = 0
    private var originalName: String = ""

    fun selectPdf(uri: Uri) {
        val fileInfo = getFileInfo(uri)
        originalName = fileInfo.first
        originalSize = fileInfo.second
        if (originalSize > 0) {
            _state.value = CompressState.Selected(uri, originalName, originalSize)
        } else {
            _state.value = CompressState.Error("Invalid PDF file or size is 0.")
        }
    }
    
    fun getOriginalName(): String = originalName

    fun compressPdf(uri: Uri, level: CompressionLevel) {
        viewModelScope.launch {
            _state.value = CompressState.Compressing(0f, "Preparing...")
            compressedFile = PdfCompressor.compressPdf(getApplication(), uri, level) { progress, msg ->
                _state.value = CompressState.Compressing(progress, msg)
            }

            if (compressedFile != null) {
                val compSize = compressedFile!!.length()
                if (compSize < originalSize) {
                    // Ready to save
                    // We don't change state to Success yet, wait for saving
                    // But we can trigger a save intent from UI, or just transition to a "Ready to Save" state
                    // Let's make the UI request the save location via SAF, then call saveToUri
                    _state.value = CompressState.Compressing(1f, "Ready to save") // Indicates compression done, waiting for save
                } else {
                    _state.value = CompressState.NotReduced("The compressed file is not smaller than the original.")
                    compressedFile?.delete()
                    compressedFile = null
                }
            } else {
                _state.value = CompressState.Error("Compression failed.")
            }
        }
    }
    
    fun isCompressionDoneAndReadyToSave(): Boolean {
        val st = _state.value
        return st is CompressState.Compressing && st.progress >= 1f && compressedFile != null
    }

    fun saveToUri(destinationUri: Uri) {
        val fileToSave = compressedFile ?: return
        viewModelScope.launch {
            _state.value = CompressState.Saving(0.5f)
            try {
                val resolver = getApplication<Application>().contentResolver
                resolver.openOutputStream(destinationUri)?.use { output ->
                    FileInputStream(fileToSave).use { input ->
                        input.copyTo(output)
                    }
                }
                
                val compSize = fileToSave.length()
                val saved = originalSize - compSize
                val percent = ((saved.toFloat() / originalSize) * 100).toInt()
                
                _state.value = CompressState.Success(
                    originalSize = originalSize,
                    compressedSize = compSize,
                    savedSize = saved,
                    reductionPercent = percent,
                    uri = destinationUri
                )
            } catch (e: Exception) {
                _state.value = CompressState.Error("Failed to save the file.")
            } finally {
                fileToSave.delete()
                compressedFile = null
            }
        }
    }

    fun reset() {
        compressedFile?.delete()
        compressedFile = null
        _state.value = CompressState.Idle
    }

    private fun getFileInfo(uri: Uri): Pair<String, Long> {
        var displayName = "document.pdf"
        var size = 0L
        try {
            if (uri.scheme == "file") {
                val file = File(uri.path ?: "")
                return Pair(file.name, file.length())
            }
            getApplication<Application>().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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
    
    override fun onCleared() {
        super.onCleared()
        compressedFile?.delete()
    }
}
