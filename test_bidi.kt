package com.spinel.pdftools
import android.text.BidiFormatter
import android.text.Layout
class BidiTest {
    fun test() {
        val isRtl = BidiFormatter.getInstance().isRtl("مرحبا")
    }
}
