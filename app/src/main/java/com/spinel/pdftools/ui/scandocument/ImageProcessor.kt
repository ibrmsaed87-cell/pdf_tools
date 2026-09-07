package com.spinel.pdftools.ui.scandocument

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageProcessor {

    fun cropAndTransform(context: Context, sourceUri: Uri, quad: Quadrilateral): Uri? {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        
        // 1. Apply EXIF rotation to match what the user saw on screen
        val exifInputStream = context.contentResolver.openInputStream(sourceUri)
        var rotatedBitmap = originalBitmap
        if (exifInputStream != null) {
            val exif = ExifInterface(exifInputStream)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            }
            if (!matrix.isIdentity) {
                rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                if (rotatedBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
            }
            exifInputStream.close()
        }

        // 2. Setup the perspective transform
        val (outWidth, outHeight) = CropGeometry.getOutputDimensions(quad)
        
        // Safety: Do not create a 0x0 or ridiculously large bitmap
        if (outWidth <= 0 || outHeight <= 0) {
            rotatedBitmap.recycle()
            return null
        }
        
        val maxDim = 4000f
        var finalWidth = outWidth
        var finalHeight = outHeight
        
        if (finalWidth > maxDim || finalHeight > maxDim) {
            val scale = maxDim / maxOf(finalWidth, finalHeight)
            finalWidth *= scale
            finalHeight *= scale
        }

        val srcPoints = floatArrayOf(
            quad.topLeft.x, quad.topLeft.y,
            quad.topRight.x, quad.topRight.y,
            quad.bottomRight.x, quad.bottomRight.y,
            quad.bottomLeft.x, quad.bottomLeft.y
        )
        
        val dstPoints = floatArrayOf(
            0f, 0f,
            finalWidth, 0f,
            finalWidth, finalHeight,
            0f, finalHeight
        )

        val perspectiveMatrix = Matrix()
        perspectiveMatrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        // 3. Apply the transform
        val outputBitmap = Bitmap.createBitmap(finalWidth.toInt(), finalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        canvas.drawBitmap(rotatedBitmap, perspectiveMatrix, Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true })
        
        rotatedBitmap.recycle()

        // 4. Save to temporary file
        val outputFile = File(context.cacheDir, "corrected_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(outputFile)
        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        
        outputBitmap.recycle()

        return Uri.fromFile(outputFile)
    }
}
