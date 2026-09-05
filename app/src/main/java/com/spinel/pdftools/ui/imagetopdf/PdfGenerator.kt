package com.spinel.pdftools.ui.imagetopdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

object PdfGenerator {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    suspend fun generatePdf(
        context: Context,
        pages: List<DocumentPage>,
        outputUri: Uri,
        onProgress: (Int, Int) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            var pdfPageNumber = 1

            for ((index, pageItem) in pages.withIndex()) {
                when (pageItem) {
                    is DocumentPage.Image -> {
                        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber).create()
                        val page = pdfDocument.startPage(pageInfo)

                        context.contentResolver.openInputStream(pageItem.uri)?.use { inputStream ->
                            val bitmap = decodeSampledBitmap(inputStream)
                            if (bitmap != null) {
                                val canvas = page.canvas
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
                        pdfDocument.finishPage(page)
                        pdfPageNumber++
                    }
                    is DocumentPage.Text -> {
                        val titleAlign = when (pageItem.titleStyle.alignment) {
                            TextAlignment.Start -> Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.End -> Layout.Alignment.ALIGN_OPPOSITE
                        }
                        val bodyAlign = when (pageItem.bodyStyle.alignment) {
                            TextAlignment.Start -> Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.End -> Layout.Alignment.ALIGN_OPPOSITE
                        }
                        
                        val titleTypeface = if (pageItem.titleStyle.isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
                        val titlePaint = TextPaint().apply {
                            color = pageItem.titleStyle.color.colorValue.toInt()
                            textSize = pageItem.titleStyle.fontSize.toFloat()
                            this.typeface = titleTypeface
                            isAntiAlias = true
                        }
                        
                        val bodyTypeface = if (pageItem.bodyStyle.isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
                        val bodyPaint = TextPaint().apply {
                            color = pageItem.bodyStyle.color.colorValue.toInt()
                            textSize = pageItem.bodyStyle.fontSize.toFloat()
                            this.typeface = bodyTypeface
                            isAntiAlias = true
                        }

                        val marginX = 50f
                        val availableWidth = PAGE_WIDTH - 2 * marginX.toInt()
                        val pageAvailableHeight = PAGE_HEIGHT - 100f

                        val titleLayout = if (pageItem.title.isNotBlank()) {
                            StaticLayout.Builder.obtain(pageItem.title, 0, pageItem.title.length, titlePaint, availableWidth)
                                .setAlignment(titleAlign)
                                .build()
                        } else null

                        val bodyLayout = if (pageItem.body.isNotBlank()) {
                            StaticLayout.Builder.obtain(pageItem.body, 0, pageItem.body.length, bodyPaint, availableWidth)
                                .setAlignment(bodyAlign)
                                .build()
                        } else null

                        var bodyStartLine = 0
                        var isFirstPageOfText = true

                        while (isFirstPageOfText || (bodyLayout != null && bodyStartLine < bodyLayout.lineCount)) {
                            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pdfPageNumber).create()
                            val page = pdfDocument.startPage(pageInfo)
                            val canvas = page.canvas

                            var currentY = 50f
                            var remainingHeightForBody = pageAvailableHeight

                            if (isFirstPageOfText && titleLayout != null) {
                                canvas.save()
                                canvas.translate(marginX, currentY)
                                titleLayout.draw(canvas)
                                canvas.restore()

                                val titleHeight = titleLayout.height + 24f
                                currentY += titleHeight
                                remainingHeightForBody -= titleHeight
                            }

                            if (bodyLayout != null && bodyStartLine < bodyLayout.lineCount) {
                                var bodyEndLine = bodyStartLine
                                var currentBodyHeight = 0
                                while (bodyEndLine < bodyLayout.lineCount) {
                                    val h = bodyLayout.getLineBottom(bodyEndLine) - bodyLayout.getLineTop(bodyEndLine)
                                    if (currentBodyHeight + h > remainingHeightForBody && bodyEndLine > bodyStartLine) {
                                        break
                                    }
                                    currentBodyHeight += h
                                    bodyEndLine++
                                }

                                val startY = bodyLayout.getLineTop(bodyStartLine).toFloat()
                                val endY = bodyLayout.getLineBottom(bodyEndLine - 1).toFloat()

                                canvas.save()
                                canvas.translate(marginX, currentY - startY)
                                canvas.clipRect(0f, startY, availableWidth.toFloat(), endY)
                                bodyLayout.draw(canvas)
                                canvas.restore()

                                bodyStartLine = bodyEndLine
                            }

                            pdfDocument.finishPage(page)
                            pdfPageNumber++
                            isFirstPageOfText = false
                        }
                    }
                }
                onProgress(index + 1, pages.size)
            }

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun decodeSampledBitmap(inputStream: InputStream): Bitmap? {
        val bytes = inputStream.readBytes()
        
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        options.inSampleSize = calculateInSampleSize(options, PAGE_WIDTH, PAGE_HEIGHT)
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
