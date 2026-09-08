package com.spinel.pdftools.ui.createpdf

import android.net.Uri
import com.spinel.pdftools.ui.imagetopdf.DocumentPage
import com.spinel.pdftools.ui.imagetopdf.TextStyleConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreatePdfViewModelTest {

    @Test
    fun `initial state is empty`() {
        val viewModel = CreatePdfViewModel()
        val state = viewModel.state.value
        assertTrue(state.pages.isEmpty())
    }

    @Test
    fun `addImages adds image pages`() {
        val viewModel = CreatePdfViewModel()
        val mockUri1 = Uri.parse("mock://uri")
        val mockUri2 = Uri.parse("mock://uri")

        viewModel.addImages(listOf(mockUri1, mockUri2))

        val state = viewModel.state.value
        assertEquals(2, state.pages.size)
        assertTrue(state.pages[0] is DocumentPage.Image)
        assertTrue(state.pages[1] is DocumentPage.Image)
    }

    @Test
    fun `saveTextPage adds new text page when no editing id`() {
        val viewModel = CreatePdfViewModel()

        viewModel.saveTextPage("Title", "Body", TextStyleConfig(), TextStyleConfig())

        val state = viewModel.state.value
        assertEquals(1, state.pages.size)
        val page = state.pages[0] as DocumentPage.Text
        assertEquals("Title", page.title)
        assertEquals("Body", page.body)
    }

    @Test
    fun `saveTextPage edits existing text page preserving id`() {
        val viewModel = CreatePdfViewModel()
        viewModel.saveTextPage("Old Title", "Old Body", TextStyleConfig(), TextStyleConfig())
        
        val firstPageId = viewModel.state.value.pages[0].id
        
        viewModel.showTextEditor(firstPageId)
        viewModel.saveTextPage("New Title", "New Body", TextStyleConfig(), TextStyleConfig())

        val state = viewModel.state.value
        assertEquals(1, state.pages.size)
        val page = state.pages[0] as DocumentPage.Text
        assertEquals("New Title", page.title)
        assertEquals("New Body", page.body)
        assertEquals(firstPageId, page.id) // ID must be preserved
    }

    @Test
    fun `removePage removes correct page`() {
        val viewModel = CreatePdfViewModel()
        viewModel.saveTextPage("1", "1", TextStyleConfig(), TextStyleConfig())
        viewModel.saveTextPage("2", "2", TextStyleConfig(), TextStyleConfig())
        
        val firstPageId = viewModel.state.value.pages[0].id
        viewModel.removePage(firstPageId)

        val state = viewModel.state.value
        assertEquals(1, state.pages.size)
        assertEquals("2", (state.pages[0] as DocumentPage.Text).title)
    }

    @Test
    fun `reorderPages swaps items correctly and preserves ids`() {
        val viewModel = CreatePdfViewModel()
        viewModel.saveTextPage("1", "1", TextStyleConfig(), TextStyleConfig())
        val mockUri = Uri.parse("mock://uri")
        viewModel.addImages(listOf(mockUri))

        val initialPages = viewModel.state.value.pages
        val id0 = initialPages[0].id
        val id1 = initialPages[1].id

        viewModel.reorderPages(0, 1)

        val newPages = viewModel.state.value.pages
        assertEquals(2, newPages.size)
        // Check order swapped
        assertEquals(id1, newPages[0].id)
        assertEquals(id0, newPages[1].id)
        // Check type swapped
        assertTrue(newPages[0] is DocumentPage.Image)
        assertTrue(newPages[1] is DocumentPage.Text)
    }
}
