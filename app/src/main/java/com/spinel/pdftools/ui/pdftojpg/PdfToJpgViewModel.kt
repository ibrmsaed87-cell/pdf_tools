package com.spinel.pdftools.ui.pdftojpg

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.pdftools.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

data class PdfPage(
    val id: String,
    val originalIndex: Int
)

sealed class PdfToJpgState {
    object Empty : PdfToJpgState()
    object Loading : PdfToJpgState()
    data class Editing(val items: List<PdfPage>, val selectedIndices: Set<Int>) : PdfToJpgState()
    data class Generating(val current: Int, val total: Int) : PdfToJpgState()
    data class ReadyToSave(val tempDir: File, val generatedCount: Int) : PdfToJpgState()
    data class Saving(val current: Int, val total: Int) : PdfToJpgState()
    data class Success(val savedCount: Int) : PdfToJpgState()
    data class Error(val messageRes: Int) : PdfToJpgState()
}

class PdfToJpgViewModel : ViewModel() {
    private val _state = MutableStateFlow<PdfToJpgState>(PdfToJpgState.Empty)
    val state: StateFlow<PdfToJpgState> = _state.asStateFlow()

    private var currentItems = mutableListOf<PdfPage>()
    private var selectedIndices = mutableSetOf<Int>()
    private var sourceCacheFile: File? = null
    private var tempOutputDir: File? = null
    private var currentPdfName: String = ""

    // PdfRenderer
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private val rendererMutex = Mutex()

    // LRU Cache for Bitmaps
    private val thumbnailCache = LruCache<Int, Bitmap>(30)

    private var generatingJob: Job? = null
    private var savingJob: Job? = null

    fun selectPdf(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = PdfToJpgState.Loading
            try {
                cleanupCurrentPdf()

                // Extract name
                var displayName = "document"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex) ?: "document"
                        }
                    }
                }
                currentPdfName = displayName.substringBeforeLast(".")
                    .replace(Regex("[^a-zA-Z0-9.\\-]"), "_") // Sanitize

                // Copy to cache safely
                val cacheDir = context.cacheDir
                val cacheFile = File(cacheDir, "pdf_to_jpg_src_${UUID.randomUUID()}.pdf")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    _state.value = PdfToJpgState.Error(R.string.err_pdf_corrupt)
                    return@launch
                }

                sourceCacheFile = cacheFile

                // Setup PdfRenderer
                fileDescriptor = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
                try {
                    pdfRenderer = PdfRenderer(fileDescriptor!!)
                } catch (e: SecurityException) {
                    _state.value = PdfToJpgState.Error(R.string.err_pdf_encrypted)
                    return@launch
                }

                val pageCount = pdfRenderer?.pageCount ?: 0
                if (pageCount == 0) {
                    _state.value = PdfToJpgState.Error(R.string.err_zero_page_pdf)
                    return@launch
                }

                // Initialize items
                currentItems.clear()
                selectedIndices.clear()
                for (i in 0 until pageCount) {
                    currentItems.add(PdfPage(id = UUID.randomUUID().toString(), originalIndex = i))
                }

                _state.value = PdfToJpgState.Editing(currentItems.toList(), selectedIndices.toSet())

            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = PdfToJpgState.Error(R.string.err_pdf_corrupt)
            }
        }
    }

    suspend fun getPageThumbnail(pageIndex: Int): Bitmap? {
        val cached = thumbnailCache.get(pageIndex)
        if (cached != null) return cached

        return withContext(Dispatchers.IO) {
            try {
                rendererMutex.withLock {
                    val renderer = pdfRenderer ?: return@withLock null
                    if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withLock null
                    
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
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun togglePageSelection(index: Int) {
        if (_state.value !is PdfToJpgState.Editing) return
        
        if (selectedIndices.contains(index)) {
            selectedIndices.remove(index)
        } else {
            if (index in 0 until currentItems.size) {
                selectedIndices.add(index)
            }
        }
        _state.value = PdfToJpgState.Editing(currentItems.toList(), selectedIndices.toSet())
    }

    fun selectAll() {
        if (_state.value !is PdfToJpgState.Editing) return
        selectedIndices.clear()
        selectedIndices.addAll(0 until currentItems.size)
        _state.value = PdfToJpgState.Editing(currentItems.toList(), selectedIndices.toSet())
    }

    fun deselectAll() {
        if (_state.value !is PdfToJpgState.Editing) return
        selectedIndices.clear()
        _state.value = PdfToJpgState.Editing(currentItems.toList(), selectedIndices.toSet())
    }

    fun convertSelectedPages(context: Context) {
        if (selectedIndices.isEmpty()) return
        val pagesToConvert = selectedIndices.sorted()
        
        generatingJob?.cancel()
        generatingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = PdfToJpgState.Generating(0, pagesToConvert.size)
                
                // Temp output dir
                val cacheDir = context.cacheDir
                val tempDir = File(cacheDir, "pdf_to_jpg_out_${UUID.randomUUID()}")
                if (!tempDir.exists()) tempDir.mkdirs()
                
                tempOutputDir?.deleteRecursively()
                tempOutputDir = tempDir
                
                // Needs a separate renderer? No, we serialize access via mutex.
                // We'll reuse the same renderer but use a large resolution.
                val totalPdfPagesStrLen = currentItems.size.toString().length
                
                var generatedCount = 0
                for ((progressIndex, pageIndex) in pagesToConvert.withIndex()) {
                    if (!isActive) throw kotlinx.coroutines.CancellationException()
                    
                    _state.value = PdfToJpgState.Generating(progressIndex + 1, pagesToConvert.size)
                    
                    rendererMutex.withLock {
                        val renderer = pdfRenderer ?: throw Exception("Renderer closed")
                        val page = renderer.openPage(pageIndex)
                        
                        try {
                            // Max long edge 2500
                            val scale = 3.0f
                            var targetWidth = (page.width * scale).toInt()
                            var targetHeight = (page.height * scale).toInt()
                            
                            val maxEdge = maxOf(targetWidth, targetHeight)
                            if (maxEdge > 2500) {
                                val shrinkRatio = 2500f / maxEdge.toFloat()
                                targetWidth = (targetWidth * shrinkRatio).toInt()
                                targetHeight = (targetHeight * shrinkRatio).toInt()
                            }
                            
                            targetWidth = maxOf(1, targetWidth)
                            targetHeight = maxOf(1, targetHeight)
                            
                            var bitmap: Bitmap? = null
                            try {
                                bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                                
                                val paddedPageNum = String.format(Locale.US, "%0${totalPdfPagesStrLen}d", pageIndex + 1)
                                val jpgFile = File(tempDir, "${currentPdfName}_page_${paddedPageNum}.jpg")
                                
                                FileOutputStream(jpgFile).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                }
                                
                                // Validate
                                if (!jpgFile.exists() || jpgFile.length() == 0L) {
                                    throw Exception("Failed to write JPG")
                                }
                                
                                // Decode bounds validation
                                val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                android.graphics.BitmapFactory.decodeFile(jpgFile.absolutePath, options)
                                if (options.outWidth <= 0 || options.outHeight <= 0) {
                                    throw Exception("Generated JPG is invalid")
                                }
                                
                                generatedCount++
                            } finally {
                                bitmap?.recycle()
                            }
                        } finally {
                            page.close()
                        }
                    }
                }
                
                if (generatedCount == pagesToConvert.size) {
                    _state.value = PdfToJpgState.ReadyToSave(tempDir, generatedCount)
                } else {
                    throw Exception("Mismatch in generated images count")
                }
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                tempOutputDir?.deleteRecursively()
                tempOutputDir = null
                _state.value = PdfToJpgState.Editing(currentItems.toList(), selectedIndices.toSet())
            } catch (e: OutOfMemoryError) {
                tempOutputDir?.deleteRecursively()
                tempOutputDir = null
                _state.value = PdfToJpgState.Error(R.string.err_failed_convert)
            } catch (e: Exception) {
                e.printStackTrace()
                tempOutputDir?.deleteRecursively()
                tempOutputDir = null
                _state.value = PdfToJpgState.Error(R.string.err_failed_convert)
            }
        }
    }
    
    fun cancelConversion() {
        generatingJob?.cancel()
    }
    
    fun saveSingleJpg(context: Context, destUri: Uri) {
        val tempDir = tempOutputDir ?: return
        val files = tempDir.listFiles()?.sortedBy { it.name } ?: return
        if (files.isEmpty()) return
        val srcFile = files.first()
        
        savingJob?.cancel()
        savingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = PdfToJpgState.Saving(1, 1)
                context.contentResolver.openOutputStream(destUri)?.use { out ->
                    srcFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }
                
                // Clean up temp
                tempOutputDir?.deleteRecursively()
                tempOutputDir = null
                
                _state.value = PdfToJpgState.Success(1)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = PdfToJpgState.Error(R.string.err_failed_save)
            }
        }
    }

    fun saveMultipleJpgs(context: Context, destTreeUri: Uri) {
        val tempDir = tempOutputDir ?: return
        val files = tempDir.listFiles()?.sortedBy { it.name } ?: return
        if (files.isEmpty()) return
        
        savingJob?.cancel()
        savingJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = PdfToJpgState.Saving(0, files.size)
                
                val docId = DocumentsContract.getTreeDocumentId(destTreeUri)
                val dirUri = DocumentsContract.buildDocumentUriUsingTree(destTreeUri, docId)
                
                var savedCount = 0
                for ((index, file) in files.withIndex()) {
                    if (!isActive) throw kotlinx.coroutines.CancellationException()
                    _state.value = PdfToJpgState.Saving(index + 1, files.size)
                    
                    var newFileName = file.name
                    // To handle collisions safely, we can rely on createDocument which often appends (1) automatically,
                    // but we can't easily check findFile without DocumentFile. Let's let the provider handle name collisions.
                    
                    val newFileUri = DocumentsContract.createDocument(context.contentResolver, dirUri, "image/jpeg", newFileName)
                        ?: throw Exception("Failed to create document")
                        
                    context.contentResolver.openOutputStream(newFileUri)?.use { out ->
                        file.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    savedCount++
                }
                
                if (savedCount == files.size) {
                    tempOutputDir?.deleteRecursively()
                    tempOutputDir = null
                    _state.value = PdfToJpgState.Success(savedCount)
                } else {
                    throw Exception("Not all images were saved")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // If cancelled by some UI
                _state.value = PdfToJpgState.ReadyToSave(tempDir, files.size)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = PdfToJpgState.Error(R.string.err_failed_save)
            }
        }
    }

    fun reset() {
        cleanupCurrentPdf()
        _state.value = PdfToJpgState.Empty
    }

    private fun cleanupCurrentPdf() {
        try {
            thumbnailCache.evictAll()
            pdfRenderer?.close()
            pdfRenderer = null
            fileDescriptor?.close()
            fileDescriptor = null
            
            sourceCacheFile?.delete()
            sourceCacheFile = null
            
            tempOutputDir?.deleteRecursively()
            tempOutputDir = null
            
            currentItems.clear()
            selectedIndices.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanupCurrentPdf()
    }
}
