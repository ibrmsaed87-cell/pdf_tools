package com.spinel.pdftools.ui.viewer

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.pdftools.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class PdfViewerState {
    object Loading : PdfViewerState()
    data class Ready(val pageCount: Int, val displayName: String) : PdfViewerState()
    data class Error(val messageRes: Int) : PdfViewerState()
}

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<PdfViewerState>(PdfViewerState.Loading)
    val state: StateFlow<PdfViewerState> = _state.asStateFlow()

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var sourceCacheFile: File? = null
    private val rendererMutex = Mutex()

    // 12.5% of max memory, up to 48MB
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = minOf(maxMemoryKb / 8, 48 * 1024)

    private val bitmapCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private val aspectRatios = mutableMapOf<Int, Float>()

    fun loadPdf(uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = PdfViewerState.Loading
            try {
                val context = getApplication<Application>()
                val uri = Uri.parse(uriString)

                // Get display name
                var displayName = "document.pdf"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex) ?: displayName
                        }
                    }
                }

                val cacheDir = context.cacheDir
                val cacheFile = File(cacheDir, "viewer_src_${UUID.randomUUID()}.pdf")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    _state.value = PdfViewerState.Error(R.string.err_corrupt_pdf)
                    return@launch
                }

                sourceCacheFile = cacheFile
                fileDescriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                
                try {
                    pdfRenderer = PdfRenderer(fileDescriptor!!)
                } catch (e: SecurityException) {
                    _state.value = PdfViewerState.Error(R.string.err_password_protected)
                    return@launch
                }

                val pageCount = pdfRenderer?.pageCount ?: 0
                if (pageCount == 0) {
                    _state.value = PdfViewerState.Error(R.string.err_corrupt_pdf)
                    return@launch
                }

                _state.value = PdfViewerState.Ready(pageCount, displayName)

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = PdfViewerState.Error(R.string.err_cannot_open_pdf)
            }
        }
    }

    suspend fun getPageAspectRatio(pageIndex: Int): Float {
        aspectRatios[pageIndex]?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    val renderer = pdfRenderer ?: return@withLock 0.707f // Default A4 approx
                    val page = renderer.openPage(pageIndex)
                    val ratio = page.width.toFloat() / page.height.toFloat()
                    page.close()
                    aspectRatios[pageIndex] = ratio
                    ratio
                }
            } catch (e: Exception) {
                0.707f
            }
        }
    }

    suspend fun getPageBitmap(pageIndex: Int, displayWidth: Int, isHighRes: Boolean = false): Bitmap? {
        val cacheKey = "${pageIndex}_$isHighRes"
        bitmapCache.get(cacheKey)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    val renderer = pdfRenderer ?: return@withLock null
                    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withLock null

                    val page = renderer.openPage(pageIndex)
                    
                    try {
                        val baseScale = displayWidth.toFloat() / page.width.toFloat()
                        val scale = if (isHighRes) baseScale * 2.0f else baseScale
                        
                        var targetWidth = (page.width * scale).toInt()
                        var targetHeight = (page.height * scale).toInt()
                        
                        // Hard limit to prevent OOM (max 2048px side)
                        val maxEdge = maxOf(targetWidth, targetHeight)
                        if (maxEdge > 2048) {
                            val shrinkRatio = 2048f / maxEdge.toFloat()
                            targetWidth = (targetWidth * shrinkRatio).toInt()
                            targetHeight = (targetHeight * shrinkRatio).toInt()
                        }
                        
                        targetWidth = maxOf(1, targetWidth)
                        targetHeight = maxOf(1, targetHeight)
                        
                        // Check byte allocation safety
                        val estimatedBytes = targetWidth.toLong() * targetHeight.toLong() * 4L
                        if (estimatedBytes > 32 * 1024 * 1024L) { // Prevent single allocation > 32MB just in case
                            return@withLock null
                        }

                        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        
                        bitmapCache.put(cacheKey, bitmap)
                        bitmap
                    } finally {
                        page.close()
                    }
                }
            } catch (e: OutOfMemoryError) {
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            bitmapCache.evictAll()
            pdfRenderer?.close()
            fileDescriptor?.close()
            sourceCacheFile?.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
