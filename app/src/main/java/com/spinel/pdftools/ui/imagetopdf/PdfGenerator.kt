package com.spinel.pdftools.ui.imagetopdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object PdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MAX_IMAGE_DIMENSION = 1600

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun generatePdf(
        context: Context,
        pages: List<DocumentPage>,
        outputUri: Uri,
        onProgress: (Int, Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()

            for ((index, pageItem) in pages.withIndex()) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                
                when (pageItem) {
                    is DocumentPage.Image -> {
                        val bitmap = loadAndScaleBitmap(context, pageItem.uri)
                        if (bitmap != null) {
                            val scale = minOf(
                                PAGE_WIDTH.toFloat() / bitmap.width,
                                PAGE_HEIGHT.toFloat() / bitmap.height
                            )
                            
                            val scaledWidth = bitmap.width * scale
                            val scaledHeight = bitmap.height * scale
                            
                            val left = (PAGE_WIDTH - scaledWidth) / 2f
                            val top = (PAGE_HEIGHT - scaledHeight) / 2f
                            
                            val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                            val dstRect = Rect(left.toInt(), top.toInt(), (left + scaledWidth).toInt(), (top + scaledHeight).toInt())
                            
                            canvas.drawBitmap(bitmap, srcRect, dstRect, null)
                            bitmap.recycle()
                        }
                    }
                    is DocumentPage.Text -> {
                        val titlePaint = TextPaint().apply {
                            color = Color.BLACK
                            textSize = 28f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            isAntiAlias = true
                        }
                        val bodyPaint = TextPaint().apply {
                            color = Color.BLACK
                            textSize = 16f
                            typeface = Typeface.DEFAULT
                            isAntiAlias = true
                        }

                        var currentY = 50f
                        val marginX = 50f
                        val availableWidth = PAGE_WIDTH - 2 * marginX.toInt()

                        if (pageItem.title.isNotBlank()) {
                            val titleLayout = StaticLayout.Builder.obtain(pageItem.title, 0, pageItem.title.length, titlePaint, availableWidth)
                                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                .build()
                            
                            canvas.save()
                            canvas.translate(marginX, currentY)
                            titleLayout.draw(canvas)
                            canvas.restore()
                            
                            currentY += titleLayout.height + 24f
                        }

                        if (pageItem.body.isNotBlank()) {
                            val bodyLayout = StaticLayout.Builder.obtain(pageItem.body, 0, pageItem.body.length, bodyPaint, availableWidth)
                                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                                .build()
                            
                            canvas.save()
                            canvas.translate(marginX, currentY)
                            bodyLayout.draw(canvas)
                            canvas.restore()
                        }
                    }
                }
                
                pdfDocument.finishPage(page)
                
                withContext(Dispatchers.Main) {
                    onProgress(index + 1, pages.size)
                }
            }

            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                pdfDocument.writeTo(out)
            } ?: throw IOException("Could not open output stream")
            
            pdfDocument.close()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun loadAndScaleBitmap(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        
        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            resolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION)
        options.inJustDecodeBounds = false

        val bitmap = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        } ?: return null

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        return if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap
        } else {
            bitmap
        }
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
