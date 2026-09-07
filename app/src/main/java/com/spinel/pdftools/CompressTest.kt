package com.spinel.pdftools

import android.graphics.Bitmap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.cos.COSName
import java.io.InputStream
import java.io.ByteArrayInputStream

class CompressTest {
    fun testCompress(doc: PDDocument) {
        val page = doc.getPage(0)
        val res = page.resources
        res.xObjectNames.forEach { name ->
            val xobj = res.getXObject(name)
            if (xobj is PDImageXObject) {
                val bmp = xobj.image
                val compressed = ByteArrayInputStream(ByteArray(0))
                val newImg = JPEGFactory.createFromStream(doc, compressed)
                res.put(name, newImg)
            }
        }
    }
}
