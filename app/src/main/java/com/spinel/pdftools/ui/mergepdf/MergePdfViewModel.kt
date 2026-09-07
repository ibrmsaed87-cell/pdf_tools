package com.spinel.pdftools.ui.mergepdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.text.DecimalFormat

data class PdfItem(
    val id: String = UUID.randomUUID().toString(),
    val originalUri: Uri,
    val name: String,
    val sizeStr: String,
    val pageCount: Int,
    val cacheFile: File
)

sealed class MergeState {
    object Empty : MergeState()
    data class SelectedFiles(val items: List<PdfItem>) : MergeState()
    object Processing : MergeState() // Used for loading files or merging
    data class Success(val savedUri: Uri) : MergeState()
    data class Error(val message: String, val arg: String = "") : MergeState()
}

class MergePdfViewModel : ViewModel() {
    private val _state = MutableStateFlow<MergeState>(MergeState.Empty)
    val state: StateFlow<MergeState> = _state.asStateFlow()

    private val currentItems = mutableListOf<PdfItem>()

    fun addItemsForTest(items: List<PdfItem>) {
        currentItems.addAll(items)
        _state.value = MergeState.SelectedFiles(currentItems.toList())
    }

    fun addPdfs(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        // Save current items to pass to state, update state to Processing
        _state.value = MergeState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                for (uri in uris) {
                    // Check for duplicates
                    if (currentItems.any { it.originalUri == uri }) continue

                    val name = getFileName(context, uri)
                    val sizeBytes = getFileSize(context, uri)
                    val sizeStr = formatFileSize(sizeBytes)
                    
                    val cacheFile = File(context.cacheDir, "merge_src_${UUID.randomUUID()}.pdf")
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open stream for $name")
                    
                    val outputStream = FileOutputStream(cacheFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    if (cacheFile.length() == 0L) {
                        cacheFile.delete()
                        throw Exception("Zero byte file: $name")
                    }

                    // Get page count and check if encrypted/corrupt
                    var pageCount = 0
                    try {
                        val document = PDDocument.load(cacheFile, MemoryUsageSetting.setupTempFileOnly())
                        if (document.isEncrypted) {
                            document.close()
                            cacheFile.delete()
                            withContext(Dispatchers.Main) {
                                _state.value = MergeState.Error("err_unable_to_read_pdf", name)
                            }
                            return@launch
                        }
                        pageCount = document.numberOfPages
                        document.close()
                    } catch (e: Exception) {
                        cacheFile.delete()
                        withContext(Dispatchers.Main) {
                            _state.value = MergeState.Error("err_unable_to_read_pdf", name)
                        }
                        return@launch
                    }

                    currentItems.add(PdfItem(originalUri = uri, name = name, sizeStr = sizeStr, pageCount = pageCount, cacheFile = cacheFile))
                }
                
                withContext(Dispatchers.Main) {
                    _state.value = MergeState.SelectedFiles(currentItems.toList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = MergeState.Error("err_unable_to_merge_pdf")
                }
            }
        }
    }

    fun removePdf(item: PdfItem) {
        val currentState = _state.value
        if (currentState is MergeState.SelectedFiles) {
            item.cacheFile.delete()
            currentItems.remove(item)
            if (currentItems.isEmpty()) {
                _state.value = MergeState.Empty
            } else {
                _state.value = MergeState.SelectedFiles(currentItems.toList())
            }
        }
    }

    fun reorderPdfs(fromIndex: Int, toIndex: Int) {
        val currentState = _state.value
        if (currentState is MergeState.SelectedFiles) {
            val item = currentItems.removeAt(fromIndex)
            currentItems.add(toIndex, item)
            _state.value = MergeState.SelectedFiles(currentItems.toList())
        }
    }

    fun mergePdfsAndSave(context: Context, destUri: Uri) {
        val currentState = _state.value
        if (currentState !is MergeState.SelectedFiles || currentItems.size < 2) return

        _state.value = MergeState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val merger = PDFMergerUtility()
                for (item in currentItems) {
                    merger.addSource(item.cacheFile)
                }
                
                val outputStream = context.contentResolver.openOutputStream(destUri)
                if (outputStream != null) {
                    merger.destinationStream = outputStream
                    merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
                    outputStream.close()
                } else {
                    throw Exception("Could not open destination stream")
                }

                withContext(Dispatchers.Main) {
                    _state.value = MergeState.Success(destUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = MergeState.Error("err_unable_to_merge_pdf")
                }
            }
        }
    }

    fun reset() {
        for (item in currentItems) {
            item.cacheFile.delete()
        }
        currentItems.clear()
        _state.value = MergeState.Empty
    }

    fun dismissError() {
        if (currentItems.isEmpty()) {
            _state.value = MergeState.Empty
        } else {
            _state.value = MergeState.SelectedFiles(currentItems.toList())
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (item in currentItems) {
            item.cacheFile.delete()
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "document.pdf"
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (index != -1) {
                        return cursor.getLong(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        return 0L
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "Unknown"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
