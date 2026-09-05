package com.spinel.pdftools.ui.imagetopdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ImageToPdfViewModelTest2 {

    @Test
    fun testUpdateBodyStyleDoesNotOverwriteTitleStyle() {
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
        
        // Emulate clicking on the item to edit
        viewModel.showTextEditor(viewModel.state.value.pages.first().id)
        
        // Emulate changing only the body style
        val newBodyStyle = bodyStyle.copy(fontSize = 24, isBold = true)
        
        viewModel.saveTextPage("Title", "Body", titleStyle, newBodyStyle)
        
        val page = viewModel.state.value.pages.first() as DocumentPage.Text
        
        // Title should remain intact
        assertEquals(30, page.titleStyle.fontSize)
        assertEquals(true, page.titleStyle.isBold)
        assertEquals(TextColor.Red, page.titleStyle.color)
        assertEquals(TextAlignment.Center, page.titleStyle.alignment)
        
        // Body should be updated
        assertEquals(24, page.bodyStyle.fontSize)
        assertEquals(true, page.bodyStyle.isBold)
        assertEquals(TextColor.Blue, page.bodyStyle.color)
        assertEquals(TextAlignment.Start, page.bodyStyle.alignment)
        
        assertNotEquals(page.titleStyle.fontSize, page.bodyStyle.fontSize)
    }
}
