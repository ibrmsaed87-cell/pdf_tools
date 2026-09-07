package com.spinel.pdftools.ui.mergepdf

import android.net.Uri
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.nio.file.Files

@RunWith(RobolectricTestRunner::class)
class MergePdfViewModelTest {

    private lateinit var viewModel: MergePdfViewModel
    private lateinit var tempFile1: File
    private lateinit var tempFile2: File
    private lateinit var tempFile3: File

    @Before
    fun setup() {
        viewModel = MergePdfViewModel()
        tempFile1 = Files.createTempFile("pdf1", ".pdf").toFile()
        tempFile2 = Files.createTempFile("pdf2", ".pdf").toFile()
        tempFile3 = Files.createTempFile("pdf3", ".pdf").toFile()
    }

    @Test
    fun testReorderPdfs() {
        val item1 = PdfItem(originalUri = Uri.parse("content://1"), name = "1.pdf", sizeStr = "1 MB", pageCount = 1, cacheFile = tempFile1)
        val item2 = PdfItem(originalUri = Uri.parse("content://2"), name = "2.pdf", sizeStr = "2 MB", pageCount = 2, cacheFile = tempFile2)
        val item3 = PdfItem(originalUri = Uri.parse("content://3"), name = "3.pdf", sizeStr = "3 MB", pageCount = 3, cacheFile = tempFile3)
        
        viewModel.addItemsForTest(listOf(item1, item2, item3))
        
        // Initial order: 1, 2, 3
        viewModel.reorderPdfs(0, 2)
        
        val state = viewModel.state.value as MergeState.SelectedFiles
        assertEquals(3, state.items.size)
        // Order should now be 2, 3, 1
        assertEquals("2.pdf", state.items[0].name)
        assertEquals("3.pdf", state.items[1].name)
        assertEquals("1.pdf", state.items[2].name)
    }

    @Test
    fun testRemovePdf() {
        val item1 = PdfItem(originalUri = Uri.parse("content://1"), name = "1.pdf", sizeStr = "1 MB", pageCount = 1, cacheFile = tempFile1)
        val item2 = PdfItem(originalUri = Uri.parse("content://2"), name = "2.pdf", sizeStr = "2 MB", pageCount = 2, cacheFile = tempFile2)
        
        viewModel.addItemsForTest(listOf(item1, item2))
        
        viewModel.removePdf(item1)
        
        val state = viewModel.state.value as MergeState.SelectedFiles
        assertEquals(1, state.items.size)
        assertEquals("2.pdf", state.items[0].name)
    }

    @Test
    fun testRemovePdf_becomesEmpty() {
        val item1 = PdfItem(originalUri = Uri.parse("content://1"), name = "1.pdf", sizeStr = "1 MB", pageCount = 1, cacheFile = tempFile1)
        
        viewModel.addItemsForTest(listOf(item1))
        viewModel.removePdf(item1)
        
        assertTrue(viewModel.state.value is MergeState.Empty)
    }
}
