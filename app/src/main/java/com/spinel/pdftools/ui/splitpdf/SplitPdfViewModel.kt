package com.spinel.pdftools.ui.splitpdf

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import java.text.DecimalFormat
import java.util.UUID

data class SplitPdfItem(
    val originalUri: Uri,
    val name: String,
    val sizeStr: String,
    val pageCount: Int,
    val cacheFile: File
)

enum class SelectionMode {
    ALL_PAGES, CUSTOM_PAGES
}

sealed class SplitState {
    object Empty : SplitState()
    data class Selected(
        val item: SplitPdfItem,
        val mode: SelectionMode = SelectionMode.ALL_PAGES,
        val customRange: String = "",
        val validationError: String? = null
    ) : SplitState()
    object Processing : SplitState()
    data class Success(val savedUri: Uri) : SplitState()
    data class Error(val message: String, val arg: String = "") : SplitState()
}

class SplitPdfViewModel : ViewModel() {
    private val _state = MutableStateFlow<SplitState>(SplitState.Empty)
    val state: StateFlow<SplitState> = _state.asStateFlow()

    fun selectPdf(context: Context, uri: Uri) {
        _state.value = SplitState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Clean up previous cache if any
                cleanObsoleteCache()

                val name = getFileName(context, uri)
                val sizeBytes = getFileSize(context, uri)
                val sizeStr = formatFileSize(sizeBytes)
                
                val cacheFile = File(context.cacheDir, "split_src_${UUID.randomUUID()}.pdf")
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open stream for $name")
                
                val outputStream = FileOutputStream(cacheFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                if (cacheFile.length() == 0L) {
                    cacheFile.delete()
                    throw Exception("Zero byte file")
                }

                var pageCount = 0
                try {
                    val document = PDDocument.load(cacheFile, MemoryUsageSetting.setupTempFileOnly())
                    if (document.isEncrypted) {
                        document.close()
                        cacheFile.delete()
                        withContext(Dispatchers.Main) {
                            _state.value = SplitState.Error("err_unable_to_read_pdf", name)
                        }
                        return@launch
                    }
                    pageCount = document.numberOfPages
                    document.close()
                } catch (e: Exception) {
                    cacheFile.delete()
                    withContext(Dispatchers.Main) {
                        _state.value = SplitState.Error("err_unable_to_read_pdf", name)
                    }
                    return@launch
                }

                val item = SplitPdfItem(originalUri = uri, name = name, sizeStr = sizeStr, pageCount = pageCount, cacheFile = cacheFile)
                withContext(Dispatchers.Main) {
                    _state.value = SplitState.Selected(item = item)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = SplitState.Error("err_unable_to_process")
                }
            }
        }
    }

    fun setSelectionMode(mode: SelectionMode) {
        val currentState = _state.value
        if (currentState is SplitState.Selected) {
            _state.value = currentState.copy(mode = mode, validationError = null)
        }
    }

    fun setCustomRange(range: String) {
        val currentState = _state.value
        if (currentState is SplitState.Selected) {
            _state.value = currentState.copy(customRange = range, validationError = null)
        }
    }

    fun extractPdf(context: Context, destUri: Uri) {
        val currentState = _state.value
        if (currentState !is SplitState.Selected) return

        val pagesToExtract = try {
            if (currentState.mode == SelectionMode.ALL_PAGES) {
                (1..currentState.item.pageCount).toList()
            } else {
                PageRangeParser.parse(currentState.customRange, currentState.item.pageCount)
            }
        } catch (e: IllegalArgumentException) {
            val errorMsg = when(e.message) {
                "empty" -> "err_at_least_one_page"
                "exceeds_max" -> "err_page_does_not_exist"
                else -> "err_invalid_page_range"
            }
            _state.value = currentState.copy(validationError = errorMsg)
            return
        }

        _state.value = SplitState.Processing

        viewModelScope.launch(Dispatchers.IO) {
            var sourceDoc: PDDocument? = null
            var destDoc: PDDocument? = null
            try {
                sourceDoc = PDDocument.load(currentState.item.cacheFile, MemoryUsageSetting.setupTempFileOnly())
                destDoc = PDDocument(MemoryUsageSetting.setupTempFileOnly())

                for (pageNum in pagesToExtract) {
                    val page = sourceDoc.getPage(pageNum - 1)
                    destDoc.importPage(page)
                }

                val outputStream = context.contentResolver.openOutputStream(destUri)
                if (outputStream != null) {
                    destDoc.save(outputStream)
                    outputStream.close()
                } else {
                    throw Exception("Could not open destination stream")
                }

                withContext(Dispatchers.Main) {
                    com.spinel.pdftools.monetization.InterstitialAdManager.recordSuccessfulOperation()
                    _state.value = SplitState.Success(destUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = SplitState.Error("err_extraction_failed")
                }
            } finally {
                sourceDoc?.close()
                destDoc?.close()
            }
        }
    }

    fun reset() {
        cleanObsoleteCache()
        _state.value = SplitState.Empty
    }

    fun dismissError() {
        // Fall back to Empty, or Selected if we had one. But error mostly happens during load/extract.
        // If we fail during load, cache is gone, so Empty.
        // If we fail during extract, we could go back to Selected.
        // To be safe, we'll reset for fatal errors. 
        cleanObsoleteCache()
        _state.value = SplitState.Empty
    }

    override fun onCleared() {
        super.onCleared()
        cleanObsoleteCache()
    }

    private fun cleanObsoleteCache() {
        val stateVal = _state.value
        if (stateVal is SplitState.Selected) {
            stateVal.item.cacheFile.delete()
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
