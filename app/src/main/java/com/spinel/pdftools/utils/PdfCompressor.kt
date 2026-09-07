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

data class CompressionDiagnostics(
    var originalSize: Long = 0L,
    var primarySize: Long = 0L,
    var totalImagesFound: Int = 0,
    var imagesRecompressed: Int = 0,
    var imagesSkipped: Int = 0,
    val filters: MutableMap<String, Int> = mutableMapOf(),
    val bitsPerComponent: MutableMap<String, Int> = mutableMapOf(),
    var fallbackEligible: Boolean = false,
    var fallbackRejectionReason: String = "",
    var fallbackStarted: Boolean = false,
    var fallbackPages: Int = 0,
    var fallbackSize: Long = 0L,
    var fallbackCompleted: Boolean = false,
        var fallbackException: String = "",
    var contentStreamsFound: Int = 0,
    var contentStreamsCompressed: Int = 0,
    var contentStreamsSkipped: Int = 0,
    var formStreamsCompressed: Int = 0,
    var monochromeFallbackBlocked: Boolean = false,
    var monochromeFallbackBlockReason: String = "",
    var finalDecision: String = ""

) {
    fun formatReport(): String {
        val filterStr = if (filters.isEmpty()) "None: 0" else filters.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        val bitsStr = if (bitsPerComponent.isEmpty()) "None: 0" else bitsPerComponent.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        return """
            Compression diagnostics
            
            Original: $originalSize bytes
            Primary: $primarySize bytes
            
            Images found: $totalImagesFound
            Recompressed: $imagesRecompressed
            Skipped: $imagesSkipped
            
            Filters:
            $filterStr
            
            Bits:
            $bitsStr
            
            Fallback eligible: ${fallbackEligible.toString().uppercase()}
            Reason: $fallbackRejectionReason
            Fallback started: ${fallbackStarted.toString().uppercase()}
            Fallback pages: $fallbackPages
            Fallback size: ${if (fallbackSize > 0) "$fallbackSize bytes" else "N/A"}
                        Fallback exception: ${if (fallbackException.isNotEmpty()) fallbackException else "None"}
            
            Content streams found: $contentStreamsFound
            Content streams Flate-compressed: $contentStreamsCompressed
            Content streams skipped (already filtered): $contentStreamsSkipped
            Form streams compressed: $formStreamsCompressed
            Monochrome fallback blocked: ${monochromeFallbackBlocked.toString().uppercase()}
            Block reason: ${if (monochromeFallbackBlockReason.isNotEmpty()) monochromeFallbackBlockReason else "N/A"}
            
            Final decision: $finalDecision
        """.trimIndent()
    }
}

data class CompressionResult(
    val file: File?,
    val diagnostics: CompressionDiagnostics
)

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
    
    fun checkEligibility(document: PDDocument): Pair<Boolean, String> {
        val pages = document.pages
        var pageNum = 1
        for (page in pages) {
            val resources = page.resources ?: continue
            if (resources.fontNames.iterator().hasNext()) {
                return Pair(false, "Font resources detected on page $pageNum")
            }
            pageNum++
        }
        return Pair(true, "")
    }
}

object PdfCompressor {
    private const val TAG = "PdfCompressor"
    
    suspend fun compressPdf(
        context: Context,
        inputUri: Uri,
        level: CompressionLevel,
        onProgress: suspend (Float, String) -> Unit
    ): CompressionResult = withContext(Dispatchers.IO) {
        val diag = CompressionDiagnostics()
        var document: PDDocument? = null
        try {
            var originalSize = Long.MAX_VALUE
            try {
                context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
                    originalSize = pfd.statSize
                    diag.originalSize = originalSize
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not get original size", e)
            }

            onProgress(0.05f, context.getString(R.string.msg_loading_pdf))
            val inputStream: InputStream = context.contentResolver.openInputStream(inputUri) ?: return@withContext CompressionResult(null, diag)
            
            document = PDDocument.load(inputStream)
            
            onProgress(0.1f, context.getString(R.string.msg_analyzing_pdf))
            
            val uniqueImages = mutableSetOf<COSStream>()
            val pages = document.pages
            
                        val eligibility = CompressionEligibilityDetector.checkEligibility(document)
            diag.fallbackEligible = eligibility.first
            diag.fallbackRejectionReason = eligibility.second
            
            val processedStreams = mutableSetOf<COSStream>()
            for (page in pages) {
                ensureActive()
                val contents = page.cosObject.getDictionaryObject(COSName.CONTENTS)
                if (contents is COSStream) {
                    diag.contentStreamsFound++
                    optimizeStreamInPlace(contents, diag, processedStreams, false)
                } else if (contents is com.tom_roush.pdfbox.cos.COSArray) {
                    for (i in 0 until contents.size()) {
                        val element = contents.getObject(i)
                        if (element is COSStream) {
                            diag.contentStreamsFound++
                            optimizeStreamInPlace(element, diag, processedStreams, false)
                        }
                    }
                }
                
                val resources = page.resources ?: continue
                for (name in resources.xObjectNames) {
                    val xObject = resources.getXObject(name)
                    if (xObject is com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject) {
                        val cosStream = xObject.cosObject
                        if (cosStream is COSStream) {
                            optimizeStreamInPlace(cosStream, diag, processedStreams, true)
                        }
                    }
                }
            }

            
            for (page in pages) {
                ensureActive()
                val resources = page.resources ?: continue
                for (name in resources.xObjectNames) {
                    val xObject = resources.getXObject(name)
                    if (xObject is PDImageXObject) {
                        if (uniqueImages.add(xObject.cosObject)) {
                            // Extract metadata for diagnostics on unique images
                            val filtersObj = xObject.cosObject.getDictionaryObject(COSName.FILTER)
                            val filterNames = mutableListOf<String>()
                            if (filtersObj is COSName) filterNames.add(filtersObj.name)
                            else if (filtersObj is com.tom_roush.pdfbox.cos.COSArray) {
                                for (i in 0 until filtersObj.size()) {
                                    val f = filtersObj.getObject(i)
                                    if (f is COSName) filterNames.add(f.name)
                                }
                            }
                            if (filterNames.isEmpty()) filterNames.add("None")
                            filterNames.forEach { f -> diag.filters[f] = diag.filters.getOrDefault(f, 0) + 1 }
                            
                            val bits = xObject.bitsPerComponent
                            val bitsStr = "${bits}-bit"
                            diag.bitsPerComponent[bitsStr] = diag.bitsPerComponent.getOrDefault(bitsStr, 0) + 1
                        }
                    }
                }
            }
            
                        val totalImages = uniqueImages.size
            diag.totalImagesFound = totalImages
            
            if (diag.fallbackEligible) {
                var efficientCount = diag.filters.getOrDefault("JBIG2Decode", 0) + diag.filters.getOrDefault("CCITTFaxDecode", 0)
                var oneBitCount = diag.bitsPerComponent.getOrDefault("1-bit", 0)
                if (efficientCount > 0) {
                    diag.fallbackEligible = false
                    diag.monochromeFallbackBlocked = true
                    diag.monochromeFallbackBlockReason = "Contains CCITT/JBIG2 images"
                } else if (totalImages > 0 && oneBitCount >= totalImages / 2) {
                    diag.fallbackEligible = false
                    diag.monochromeFallbackBlocked = true
                    diag.monochromeFallbackBlockReason = "Predominantly 1-bit images ($oneBitCount / $totalImages)"
                }
            }
            
            val processedMap = mutableMapOf<COSStream, PDImageXObject>()
            var imagesProcessed = 0 // Represents images that are actually recompressed
            
            for (page in pages) {
                ensureActive()
                val resources = page.resources ?: continue
                
                val xObjectNames = resources.xObjectNames.toList()
                for (name in xObjectNames) {
                    ensureActive()
                    val xObject = resources.getXObject(name)
                    if (xObject is PDImageXObject) {
                        val isEfficient = CompressionEligibilityDetector.isEfficientFormat(
                            xObject.bitsPerComponent, 
                            xObject.cosObject.getDictionaryObject(COSName.FILTER)
                        )
                        
                        val cosStream = xObject.cosObject
                        
                        if (processedMap.containsKey(cosStream)) {
                            resources.put(name, processedMap[cosStream])
                        } else {
                            if (isEfficient) {
                                diag.imagesSkipped++
                                processedMap[cosStream] = xObject // cache original to avoid recounting
                            } else {
                                diag.imagesRecompressed++
                                imagesProcessed++
                                // We might not know total recompressed count upfront, but we can estimate
                                val expectedRecompressed = max(1, totalImages - diag.imagesSkipped)
                                val progress = 0.1f + (0.7f * (imagesProcessed.toFloat() / max(1, expectedRecompressed)))
                                
                                onProgress(progress, context.getString(R.string.msg_compressing_image, imagesProcessed, expectedRecompressed))
                                
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
                                    Log.e(TAG, "Failed to compress image", e)
                                }
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
            document = null 
            
                        val primarySize = primaryFile.length()
            diag.primarySize = primarySize

            if (isMeaningfulReduction(originalSize, primarySize)) {
                diag.finalDecision = "PRIMARY_SELECTED"
                return@withContext CompressionResult(primaryFile, diag)
            }

            if (!diag.fallbackEligible) {
                primaryFile.delete()
                diag.finalDecision = "NOT_REDUCED"
                return@withContext CompressionResult(null, diag) 
            }
            
            diag.fallbackStarted = true
            onProgress(0.9f, context.getString(R.string.msg_analyzing_pdf))
            
            val fallbackFile = File(context.cacheDir, "fallback_${System.currentTimeMillis()}.pdf")
            var fallbackSuccess = false
            
            try {
                context.contentResolver.openFileDescriptor(inputUri, "r")?.use { pfd ->
                    val renderer = android.graphics.pdf.PdfRenderer(pfd)
                    val newDoc = PDDocument()
                    val pageCount = renderer.pageCount
                    
                    for (i in 0 until pageCount) {
                        ensureActive()
                        diag.fallbackPages = i + 1
                        val progress = 0.9f + (0.1f * (i.toFloat() / max(1, pageCount)))
                        onProgress(progress, context.getString(R.string.msg_compressing_image, i + 1, pageCount))
                        
                        val page = renderer.openPage(i)
                        
                        val ptsWidth = page.width
                        val ptsHeight = page.height
                        val longestSide = max(ptsWidth, ptsHeight)
                        val scale = level.maxDimension.toFloat() / longestSide
                        val renderScale = min(scale, 4.0f)
                        
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
                    diag.fallbackCompleted = true
                }
            } catch (e: CancellationException) {
                fallbackFile.delete()
                throw e
            } catch (e: OutOfMemoryError) {
                diag.fallbackException = "OutOfMemoryError: ${e.message}"
                fallbackFile.delete()
                fallbackSuccess = false
            } catch (e: Exception) {
                diag.fallbackException = "${e.javaClass.simpleName}: ${e.message}"
                fallbackFile.delete()
                fallbackSuccess = false
            }
            
                        val fallbackSize = if (fallbackSuccess && fallbackFile.exists()) fallbackFile.length() else 0L
            diag.fallbackSize = fallbackSize

            if (fallbackSuccess && isMeaningfulReduction(originalSize, fallbackSize) && fallbackSize < primarySize) {
                primaryFile.delete()
                diag.finalDecision = "FALLBACK_SELECTED"
                return@withContext CompressionResult(fallbackFile, diag)
            } else {
                fallbackFile.delete()
                primaryFile.delete()
                diag.finalDecision = "NOT_REDUCED"
                return@withContext CompressionResult(null, diag)
            }
            
        } catch (e: CancellationException) {
            diag.finalDecision = "ERROR_CANCELLED"
            throw e
        } catch (e: OutOfMemoryError) {
            diag.finalDecision = "ERROR_OOM"
            return@withContext CompressionResult(null, diag)
        } catch (e: Exception) {
            diag.finalDecision = "ERROR_EXCEPTION"
            return@withContext CompressionResult(null, diag)
        } finally {
            try { document?.close() } catch (e: Exception) {}
        }
    }

    private fun isMeaningfulReduction(original: Long, newSize: Long): Boolean {
        if (newSize <= 0 || newSize >= original) return false
        val savedBytes = original - newSize
        return (savedBytes.toDouble() / original.toDouble()) >= 0.01
    }

    private fun optimizeStreamInPlace(stream: COSStream, diag: CompressionDiagnostics, processed: MutableSet<COSStream>, isForm: Boolean = false) {
        if (!processed.add(stream)) return
        val filters = stream.getDictionaryObject(COSName.FILTER)
        if (filters != null) {
            diag.contentStreamsSkipped++
            return
        }
        try {
            val bytes = stream.createInputStream().readBytes()
            stream.createOutputStream(COSName.FLATE_DECODE).use { os ->
                os.write(bytes)
            }
            if (isForm) diag.formStreamsCompressed++ else diag.contentStreamsCompressed++
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize stream", e)
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
