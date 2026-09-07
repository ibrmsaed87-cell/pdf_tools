package com.spinel.pdftools

import org.junit.Test

class LoadTest {
    @Test
    fun testLoadClass() {
        try {
            Class.forName("com.spinel.pdftools.ui.imagetopdf.ImageToPdfScreenKt")
            println("Class loaded successfully!")
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }
}
