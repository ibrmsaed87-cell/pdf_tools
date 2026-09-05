package com.spinel.pdftools.ui.imagetopdf

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageToPdfViewModelTest {

    @Test
    fun testSaveTextPageSavesIndependentStyles() {
        val viewModel = ImageToPdfViewModel()
        
        val titleStyle = TextStyleConfig(
            fontSize = 30,
            isBold = true,
            color = TextColor.Red,
            alignment = TextAlignment.Center
        )
        
        val bodyStyle = TextStyleConfig(
            fontSize = 16,
            isBold = false,
            color = TextColor.Blue,
            alignment = TextAlignment.Start
        )
        
        viewModel.saveTextPage("Title", "Body", titleStyle, bodyStyle)
        
        val page = viewModel.state.value.pages.first() as DocumentPage.Text
        
        assertEquals(30, page.titleStyle.fontSize)
        assertEquals(true, page.titleStyle.isBold)
        assertEquals(TextColor.Red, page.titleStyle.color)
        assertEquals(TextAlignment.Center, page.titleStyle.alignment)
        
        assertEquals(16, page.bodyStyle.fontSize)
        assertEquals(false, page.bodyStyle.isBold)
        assertEquals(TextColor.Blue, page.bodyStyle.color)
        assertEquals(TextAlignment.Start, page.bodyStyle.alignment)
    }
}
