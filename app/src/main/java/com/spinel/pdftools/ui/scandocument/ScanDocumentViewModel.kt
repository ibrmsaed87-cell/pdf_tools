package com.spinel.pdftools.ui.scandocument

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executor

data class ScannedPage(val uri: Uri, val id: String = UUID.randomUUID().toString())

sealed class ScanState {
    object Camera : ScanState()
    data class Preview(val uri: Uri) : ScanState()
    data class Cropping(val uri: Uri, val initialQuad: Quadrilateral? = null) : ScanState()
    data class CorrectedPreview(val originalUri: Uri, val correctedUri: Uri, val quad: Quadrilateral) : ScanState()
    object ViewingDocument : ScanState()
    data class SavingPdf(val progress: Float) : ScanState()
    data class Success(val uri: Uri) : ScanState()
}

class ScanDocumentViewModel : ViewModel() {
    private val _pages = MutableStateFlow<List<ScannedPage>>(emptyList())
    val pages = _pages.asStateFlow()

    private val _state = MutableStateFlow<ScanState>(ScanState.Camera)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun capturePhoto(
        imageCapture: ImageCapture,
        context: Context,
        executor: Executor
    ) {
        val photoFile = File(
            context.cacheDir,
            "scan_${System.currentTimeMillis()}.jpg"
        )
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    _state.value = ScanState.Preview(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                }
            }
        )
    }

    fun retakePhoto() {
        val currentState = _state.value
        if (currentState is ScanState.Preview) {
            deleteFileSafely(currentState.uri)
        } else if (currentState is ScanState.Cropping) {
            deleteFileSafely(currentState.uri)
        } else if (currentState is ScanState.CorrectedPreview) {
            deleteFileSafely(currentState.originalUri)
            deleteFileSafely(currentState.correctedUri)
        }
        
        _state.value = ScanState.Camera
    }
    
    fun cancelCamera() {
        if (_pages.value.isNotEmpty()) {
            _state.value = ScanState.ViewingDocument
        }
    }

    fun startCrop() {
        val currentState = _state.value
        if (currentState is ScanState.Preview) {
            _state.value = ScanState.Cropping(currentState.uri, null)
        }
    }

    fun applyCrop(context: Context, uri: Uri, quad: Quadrilateral) {
        viewModelScope.launch(Dispatchers.IO) {
            val correctedUri = ImageProcessor.cropAndTransform(context, uri, quad)
            if (correctedUri != null) {
                _state.value = ScanState.CorrectedPreview(uri, correctedUri, quad)
            } else {
                _state.value = ScanState.Cropping(uri, quad)
            }
        }
    }

    fun editCrop() {
        val currentState = _state.value
        if (currentState is ScanState.CorrectedPreview) {
            deleteFileSafely(currentState.correctedUri)
            _state.value = ScanState.Cropping(currentState.originalUri, currentState.quad)
        }
    }

    fun acceptScan() {
        val currentState = _state.value
        if (currentState is ScanState.CorrectedPreview) {
            val newPage = ScannedPage(currentState.correctedUri)
            _pages.update { it + newPage }
            deleteFileSafely(currentState.originalUri)
            _state.value = ScanState.ViewingDocument
        }
    }
    
    fun requestAddPage() {
        _state.value = ScanState.Camera
    }
    
    fun removePage(id: String) {
        val pageToRemove = _pages.value.find { it.id == id }
        pageToRemove?.let { deleteFileSafely(it.uri) }
        
        _pages.update { current -> current.filter { it.id != id } }
        
        if (_pages.value.isEmpty()) {
            _state.value = ScanState.Camera
        }
    }
    
    fun reorderPages(fromIndex: Int, toIndex: Int) {
        _pages.update { current ->
            val list = current.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
            }
            list
        }
    }

    fun reset() {
        _pages.value.forEach { deleteFileSafely(it.uri) }
        _pages.value = emptyList()
        _state.value = ScanState.Camera
    }

    private fun deleteFileSafely(uri: Uri) {
        try {
            val file = File(uri.path ?: "")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getDefaultFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "Scanned_Document_$timeStamp.pdf"
    }

    fun generatePdf(context: Context, destUri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        val pagesToExport = _pages.value
        if (pagesToExport.isEmpty()) {
            onError()
            return
        }
        
        _state.value = ScanState.SavingPdf(0f)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pdfDocument = PdfDocument()
                val PAGE_WIDTH = 595
                val PAGE_HEIGHT = 842
                
                for ((index, pageItem) in pagesToExport.withIndex()) {
                    val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                    val pdfPage = pdfDocument.startPage(pageInfo)
                    
                    context.contentResolver.openInputStream(pageItem.uri)?.use { inputStream ->
                        val bitmap = decodeSampledBitmap(inputStream, PAGE_WIDTH, PAGE_HEIGHT)
                        if (bitmap != null) {
                            val canvas = pdfPage.canvas
                            val matrix = Matrix()
                            val scale = Math.min(
                                PAGE_WIDTH.toFloat() / bitmap.width,
                                PAGE_HEIGHT.toFloat() / bitmap.height
                            )
                            val dx = (PAGE_WIDTH - bitmap.width * scale) / 2f
                            val dy = (PAGE_HEIGHT - bitmap.height * scale) / 2f
                            matrix.postScale(scale, scale)
                            matrix.postTranslate(dx, dy)
                            canvas.drawBitmap(bitmap, matrix, null)
                            bitmap.recycle()
                        }
                    }
                    
                    pdfDocument.finishPage(pdfPage)
                    
                    _state.value = ScanState.SavingPdf((index + 1).toFloat() / pagesToExport.size)
                }
                
                context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
                pdfDocument.close()
                
                launch(Dispatchers.Main) {
                    _state.value = ScanState.Success(destUri)
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    onError()
                    _state.value = ScanState.ViewingDocument
                }
            }
        }
    }
    
    private fun decodeSampledBitmap(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        val bytes = inputStream.readBytes()
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
