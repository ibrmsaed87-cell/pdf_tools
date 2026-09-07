package com.spinel.pdftools.ui.compresspdf

import com.spinel.pdftools.utils.CompressionEligibilityDetector
import com.tom_roush.pdfbox.cos.COSArray
import com.tom_roush.pdfbox.cos.COSName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompressionEligibilityDetectorTest {

    @Test
    fun testIsEfficientFormat_1Bit() {
        assertTrue(CompressionEligibilityDetector.isEfficientFormat(1, null))
    }

    @Test
    fun testIsEfficientFormat_JBIG2() {
        assertTrue(CompressionEligibilityDetector.isEfficientFormat(8, COSName.JBIG2_DECODE))
    }

    @Test
    fun testIsEfficientFormat_CCITT() {
        assertTrue(CompressionEligibilityDetector.isEfficientFormat(8, COSName.CCITTFAX_DECODE))
    }

    @Test
    fun testIsEfficientFormat_ArrayWithJBIG2() {
        val array = COSArray()
        array.add(COSName.FLATE_DECODE)
        array.add(COSName.JBIG2_DECODE)
        
        assertTrue(CompressionEligibilityDetector.isEfficientFormat(8, array))
    }

    @Test
    fun testIsEfficientFormat_JPEG() {
        assertFalse(CompressionEligibilityDetector.isEfficientFormat(8, COSName.DCT_DECODE))
    }
}
