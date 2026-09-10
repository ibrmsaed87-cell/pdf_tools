package com.spinel.pdftools.ui.organizepdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class PageItem(
    val id: String,
    val originalIndex: Int,
    val rotationDelta: Int = 0 // 0, 90, 180, 270
)

sealed class OrganizeState {
    object Empty : OrganizeState()
    object Loading : OrganizeState()
    data class Editing(val items: List<PageItem>) : OrganizeState()
    data class Generating(val current: Int, val total: Int) : OrganizeState()
    data class ReadyToSave(val tempMergedFile: File) : OrganizeState()
    data class Success(val savedUri: Uri) : OrganizeState()
    data class Error(val messageRes: String) : OrganizeState()
}

class OrganizePdfViewModel : ViewModel() {
    private val _state = MutableStateFlow<OrganizeState>(OrganizeState.Empty)
    val state: StateFlow<OrganizeState> = _state.asStateFlow()

    private var currentItems = mutableListOf<PageItem>()
    private var sourceCacheFile: File? = null
    private var tempOutFile: File? = null

    // PdfRenderer
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private val rendererMutex = Mutex()
    
    // LRU Cache for Bitmaps
    private val thumbnailCache = LruCache<Int, Bitmap>(30) // max 30 thumbnails

    fun setItemsForTest(items: List<PageItem>) {
        currentItems.clear()
        currentItems.addAll(items)
        _state.value = OrganizeState.Editing(currentItems.toList())
    }

    fun selectPdf(context: Context, uri: Uri) {
        _state.value = OrganizeState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cleanupInternal() // Clear previous if any
                
                val cacheFile = File(context.cacheDir, "organize_src_${UUID.randomUUID()}.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    throw Exception("File empty or failed to copy")
                }
                
                // Validate PDF with PDFBox
                var pageCount = 0
                try {
                    val document = PDDocument.load(cacheFile, MemoryUsageSetting.setupTempFileOnly())
                    if (document.isEncrypted) {
                        document.close()
                        cacheFile.delete()
                        withContext(Dispatchers.Main) {
                            _state.value = OrganizeState.Error("err_pdf_encrypted")
                        }
                        return@launch
                    }
                    pageCount = document.numberOfPages
                    document.close()
                } catch (e: Exception) {
                    cacheFile.delete()
                    withContext(Dispatchers.Main) {
                        _state.value = OrganizeState.Error("err_pdf_corrupt")
                    }
                    return@launch
                }
                
                if (pageCount == 0) {
                    cacheFile.delete()
                    withContext(Dispatchers.Main) {
                        _state.value = OrganizeState.Error("err_zero_page_pdf")
                    }
                    return@launch
                }
                
                sourceCacheFile = cacheFile
                
                // Setup PdfRenderer
                fileDescriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor!!)
                
                // Initialize items
                currentItems.clear()
                for (i in 0 until pageCount) {
                    currentItems.add(PageItem(id = UUID.randomUUID().toString(), originalIndex = i))
                }
                
                withContext(Dispatchers.Main) {
                    _state.value = OrganizeState.Editing(currentItems.toList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = OrganizeState.Error("err_pdf_corrupt")
                }
            }
        }
    }
    
    suspend fun getThumbnail(pageIndex: Int): Bitmap? {
        thumbnailCache.get(pageIndex)?.let { return it }
        return rendererMutex.withLock {
            // double check
            thumbnailCache.get(pageIndex)?.let { return@withLock it }
            try {
                val renderer = pdfRenderer ?: return@withLock null
                val page = renderer.openPage(pageIndex)
                val width = 400
                val height = (400f * page.height / page.width).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                thumbnailCache.put(pageIndex, bitmap)
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    fun reorderItems(fromIndex: Int, toIndex: Int) {
        val currentState = _state.value
        if (currentState is OrganizeState.Editing) {
            val item = currentItems.removeAt(fromIndex)
            currentItems.add(toIndex, item)
            _state.value = OrganizeState.Editing(currentItems.toList())
        }
    }

    fun deleteItem(id: String) {
        val currentState = _state.value
        if (currentState is OrganizeState.Editing) {
            if (currentItems.size <= 1) {
                // Should be prevented by UI or shown via Toast, but handled here as well
                return
            }
            currentItems.removeAll { it.id == id }
            _state.value = OrganizeState.Editing(currentItems.toList())
        }
    }
    
    fun rotateItem(id: String) {
        val currentState = _state.value
        if (currentState is OrganizeState.Editing) {
            val index = currentItems.indexOfFirst { it.id == id }
            if (index != -1) {
                val item = currentItems[index]
                val newDelta = (item.rotationDelta + 90) % 360
                currentItems[index] = item.copy(rotationDelta = newDelta)
                _state.value = OrganizeState.Editing(currentItems.toList())
            }
        }
    }

    fun generatePdf(context: Context) {
        val currentState = _state.value
        if (currentState !is OrganizeState.Editing) return
        
        val itemsToExport = currentItems.toList()
        if (itemsToExport.isEmpty()) return
        
        val srcFile = sourceCacheFile ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                tempOutFile?.delete()
                val tempOut = File(context.cacheDir, "organize_out_${UUID.randomUUID()}.pdf")
                
                val destDoc = PDDocument(MemoryUsageSetting.setupTempFileOnly())
                val sourceDoc = PDDocument.load(srcFile, MemoryUsageSetting.setupTempFileOnly())
                
                for (i in itemsToExport.indices) {
                    withContext(Dispatchers.Main) {
                        _state.value = OrganizeState.Generating(i + 1, itemsToExport.size)
                    }
                    val item = itemsToExport[i]
                    val sourcePage = sourceDoc.getPage(item.originalIndex)
                    val importedPage = destDoc.importPage(sourcePage)
                    
                    val currentRotation = sourcePage.rotation
                    val finalRotation = (currentRotation + item.rotationDelta) % 360
                    importedPage.rotation = finalRotation
                }
                
                destDoc.save(tempOut)
                destDoc.close()
                sourceDoc.close()
                
                if (!tempOut.exists() || tempOut.length() == 0L) {
                    throw Exception("Output file is empty")
                }
                
                // Validate
                val verifyDoc = PDDocument.load(tempOut, MemoryUsageSetting.setupTempFileOnly())
                val outPages = verifyDoc.numberOfPages
                verifyDoc.close()
                
                if (outPages != itemsToExport.size) {
                    throw Exception("Page count mismatch")
                }
                
                tempOutFile = tempOut
                
                withContext(Dispatchers.Main) {
                    _state.value = OrganizeState.ReadyToSave(tempOut)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = OrganizeState.Error("err_failed_organize")
                }
            }
        }
    }

    fun savePdf(context: Context, destUri: Uri) {
        val currentTempFile = tempOutFile ?: return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputStream = context.contentResolver.openOutputStream(destUri)
                if (outputStream != null) {
                    val inputStream = currentTempFile.inputStream()
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    withContext(Dispatchers.Main) {
                        com.spinel.pdftools.monetization.InterstitialAdManager.recordSuccessfulOperation()
                        _state.value = OrganizeState.Success(destUri)
                    }
                } else {
                    throw Exception("Could not open destination stream")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.value = OrganizeState.Error("err_failed_organize")
                }
            }
        }
    }

    fun dismissError() {
        if (currentItems.isNotEmpty()) {
            _state.value = OrganizeState.Editing(currentItems.toList())
        } else {
            _state.value = OrganizeState.Empty
        }
    }

    fun reset() {
        viewModelScope.launch(Dispatchers.IO) {
            cleanupInternal()
            withContext(Dispatchers.Main) {
                _state.value = OrganizeState.Empty
            }
        }
    }

    private suspend fun cleanupInternal() {
        rendererMutex.withLock {
            pdfRenderer?.close()
            pdfRenderer = null
            fileDescriptor?.close()
            fileDescriptor = null
            thumbnailCache.evictAll()
        }
        sourceCacheFile?.delete()
        sourceCacheFile = null
        tempOutFile?.delete()
        tempOutFile = null
        currentItems.clear()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            cleanupInternal()
        }
    }
}
