package com.spinel.pdftools.ui.pdftojpg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class PdfToJpgViewModelTest {

    private lateinit var viewModel: PdfToJpgViewModel

    @Before
    fun setup() {
        viewModel = PdfToJpgViewModel()
    }

    @Test
    fun `initial state is Empty`() = runTest {
        val state = viewModel.state.first()
        assertTrue(state is PdfToJpgState.Empty)
    }

    // Additional tests for logic could be added here, 
    // but the ViewModel methods like selectPdf rely heavily on Android Context and ContentResolver.
    // Full testing would require mocking Context, which is better suited for Robolectric tests.
}
