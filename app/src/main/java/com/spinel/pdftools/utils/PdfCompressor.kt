package com.spinel.pdftools.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.spinel.pdftools.R
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.cos.COSStream
import com.tom_roush.pdfbox.cos.COSBase
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

enum class CompressionLevel(
    val quality: Int,
    val maxDimension: Int
) {
    LOW(80, 1500),
    MEDIUM(60, 1000),
    HIGH(40, 800)
}

object CompressionEligibilityDetector {
    fun isEfficientFormat(bitsPerComponent: Int, filters: COSBase?): Boolean {
        if (bitsPerComponent == 1) return true
        val filterNames = mutableListOf<String>()
        if (filters is COSName) {
            filterNames.add(filters.name)
        } else if (filters is com.tom_roush.pdfbox.cos.COSArray) {
            for (i in 0 until filters.size()) {
                val f = filters.getObject(i)
                if (f is COSName) filterNames.add(f.name)
            }
        }
        for (name in filterNames) {
            if (name == COSName.JBIG2_DECODE.name || name == COSName.CCITTFAX_DECODE.name) {
                return true
            }
        }
        return false
    }
    
    fun isEligibleForFallback(document: PDDocument): Boolean {
        // Conservative check: if there are any fonts, it's not purely a scanned PDF
        val pages = document.pages
        for (page in pages) {
            val resources = page.resources ?: continue
            if (resources.fontNames.iterator().hasNext()) {
                return false
            }
        }
        return true
    }
}

object PdfCompressor {
    private const val TAG = "PdfCompressor"
    
    suspend fun compressPdf(
        context: Context,
        inputUri: Uri,
        level: CompressionLevel,
        onProgress: suspend (Float, String) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        var document: PDDocument? = null
        val tStart = System.currentTimeMillis()
        try {
            var originalSize = Long.MAX_VALUE
            try {
                context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
                    originalSize = pfd.statSize
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not get original size", e)
            }

            onProgress(0.05f, context.getString(R.string.msg_loading_pdf))
            val inputStream: InputStream = context.contentResolver.openInputStream(inputUri) ?: return@withContext null
            
            val tLoadStart = System.currentTimeMillis()
            document = PDDocument.load(inputStream)
            val tLoadEnd = System.currentTimeMillis()
            Log.d(TAG, "PDDocument.load took ${tLoadEnd - tLoadStart}ms")
            
            onProgress(0.1f, context.getString(R.string.msg_analyzing_pdf))
            
            val uniqueImages = mutableSetOf<COSStream>()
            val pages = document.pages
            
            // Check fallback eligibility while analyzing pages
            val isEligibleForFallback = CompressionEligibilityDetector.isEligibleForFallback(document)
            
            for (page in pages) {
                ensureActive()
                val resources = page.resources ?: continue
                for (name in resources.xObjectNames) {
                    val xObject = resources.getXObject(name)
                    if (xObject is PDImageXObject) {
                        uniqueImages.add(xObject.cosObject)
                    }
                }
            }
            
            val totalImages = uniqueImages.size
            Log.d(TAG, "Found $totalImages unique images. Eligible for fallback: $isEligibleForFallback")
            
            val processedMap = mutableMapOf<COSStream, PDImageXObject>()
            var imagesProcessed = 0
            
            for (page in pages) {
                ensureActive()
                val resources = page.resources ?: continue
                
                val xObjectNames = resources.xObjectNames.toList()
                for (name in xObjectNames) {
                    ensureActive()
                    val xObject = resources.getXObject(name)
                    if (xObject is PDImageXObject) {
                        if (CompressionEligibilityDetector.isEfficientFormat(xObject.bitsPerComponent, xObject.cosObject.getDictionaryObject(COSName.FILTER))) {
                            Log.d(TAG, "Skipping efficient image format")
                            continue
                        }
                        
                        val cosStream = xObject.cosObject
                        
                        if (processedMap.containsKey(cosStream)) {
                            resources.put(name, processedMap[cosStream])
                        } else {
                            imagesProcessed++
                            val progress = 0.1f + (0.7f * (imagesProcessed.toFloat() / max(1, totalImages)))
                            onProgress(progress, context.getString(R.string.msg_compressing_image, imagesProcessed, totalImages))
                            
                            try {
                                val originalBitmap = xObject.image
                                val compressedBitmap = compressBitmap(originalBitmap, level)
                                
                                val outStream = ByteArrayOutputStream()
                                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, level.quality, outStream)
                                val compressedBytes = outStream.toByteArray()
                                
                                val newImage = JPEGFactory.createFromStream(
                                    document,
                                    ByteArrayInputStream(compressedBytes)
                                )
                                
                                resources.put(name, newImage)
                                processedMap[cosStream] = newImage
                                
                                if (originalBitmap != compressedBitmap) {
                                    compressedBitmap.recycle()
                                }
                                originalBitmap.recycle()
                                
                                yield()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: OutOfMemoryError) {
                                Log.e(TAG, "OOM during primary compression", e)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to compress image $imagesProcessed", e)
                            }
                        }
                    }
                }
            }
            
            onProgress(0.85f, context.getString(R.string.msg_saving_pdf))
            val primaryFile = File(context.cacheDir, "primary_${System.currentTimeMillis()}.pdf")
            val fos = FileOutputStream(primaryFile)
            document.save(fos)
            fos.close()
            document.close()
            document = null // Free memory before fallback
            
            val primarySize = primaryFile.length()
            Log.d(TAG, "Primary size: $primarySize vs Original size: $originalSize")
            
            if (primarySize < originalSize) {
                return@withContext primaryFile
            }
            
            if (!isEligibleForFallback) {
                Log.d(TAG, "Document has fonts or is not eligible, skipping destructive fallback rasterization.")
                return@withContext primaryFile // ViewModel will reject this and show NotReduced
            }
            
            Log.d(TAG, "Primary failed to reduce size. Document is eligible for Fallback. Starting Fallback...")
            onProgress(0.9f, context.getString(R.string.msg_analyzing_pdf)) // Fallback indicator
            
            val fallbackFile = File(context.cacheDir, "fallback_${System.currentTimeMillis()}.pdf")
            var fallbackSuccess = false
            
            try {
                context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    val newDoc = PDDocument()
                    val pageCount = renderer.pageCount
                    
                    for (i in 0 until pageCount) {
                        ensureActive()
                        val progress = 0.9f + (0.1f * (i.toFloat() / max(1, pageCount)))
                        onProgress(progress, context.getString(R.string.msg_compressing_image, i + 1, pageCount))
                        
                        val page = renderer.openPage(i)
                        
                        val ptsWidth = page.width
                        val ptsHeight = page.height
                        val longestSide = max(ptsWidth, ptsHeight)
                        val scale = level.maxDimension.toFloat() / longestSide
                        val renderScale = min(scale, 4.0f) // limit extreme upscale
                        
                        val renderWidth = max(1, (ptsWidth * renderScale).toInt())
                        val renderHeight = max(1, (ptsHeight * renderScale).toInt())
                        
                        val bitmap = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        
                        page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()
                        
                        val outStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, level.quality, outStream)
                        bitmap.recycle()
                        
                        val jpegImage = JPEGFactory.createFromStream(newDoc, ByteArrayInputStream(outStream.toByteArray()))
                        val pdPage = com.tom_roush.pdfbox.pdmodel.PDPage(com.tom_roush.pdfbox.pdmodel.common.PDRectangle(ptsWidth.toFloat(), ptsHeight.toFloat()))
                        newDoc.addPage(pdPage)
                        
                        val contentStream = com.tom_roush.pdfbox.pdmodel.PDPageContentStream(newDoc, pdPage)
                        contentStream.drawImage(jpegImage, 0f, 0f, ptsWidth.toFloat(), ptsHeight.toFloat())
                        contentStream.close()
                    }
                    
                    newDoc.save(FileOutputStream(fallbackFile))
                    newDoc.close()
                    renderer.close()
                    fallbackSuccess = true
                }
            } catch (e: CancellationException) {
                fallbackFile.delete()
                throw e
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "OOM during fallback compression", e)
                fallbackFile.delete()
                fallbackSuccess = false
            } catch (e: Exception) {
                Log.e(TAG, "Fallback failed", e)
                fallbackFile.delete()
                fallbackSuccess = false
            }
            
            val fallbackSize = if (fallbackSuccess && fallbackFile.exists()) fallbackFile.length() else Long.MAX_VALUE
            Log.d(TAG, "Fallback size: $fallbackSize vs Primary size: $primarySize vs Original size: $originalSize")
            
            if (fallbackSuccess && fallbackSize < primarySize && fallbackSize < originalSize) {
                primaryFile.delete()
                return@withContext fallbackFile
            } else {
                fallbackFile.delete()
                return@withContext primaryFile // Returns primary, which is larger than original, ViewModel handles it
            }
            
        } catch (e: CancellationException) {
            Log.d(TAG, "Compression was cancelled by the user")
            throw e
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "OOM during compression", e)
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Compression failed", e)
            return@withContext null
        } finally {
            try { document?.close() } catch (e: Exception) {}
        }
    }

    private fun compressBitmap(original: Bitmap, level: CompressionLevel): Bitmap {
        val width = original.width
        val height = original.height
        val longestSide = max(width, height)
        
        if (longestSide > level.maxDimension) {
            val ratio = level.maxDimension.toFloat() / longestSide
            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()
            return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
        }
        
        return original
    }
}
