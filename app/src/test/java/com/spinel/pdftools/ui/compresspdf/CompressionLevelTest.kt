package com.spinel.pdftools.ui.compresspdf

import com.spinel.pdftools.utils.CompressionLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompressionLevelTest {

    @Test
    fun testCompressionLevelsDiffer() {
        val low = CompressionLevel.LOW
        val medium = CompressionLevel.MEDIUM
        val high = CompressionLevel.HIGH

        // LOW should have higher quality than MEDIUM and HIGH
        assertTrue(low.quality > medium.quality)
        assertTrue(medium.quality > high.quality)

        // LOW should have higher maxDimension than MEDIUM and HIGH
        assertTrue(low.maxDimension > medium.maxDimension)
        assertTrue(medium.maxDimension > high.maxDimension)
        
        assertEquals(80, low.quality)
        assertEquals(40, high.quality)
    }
}
