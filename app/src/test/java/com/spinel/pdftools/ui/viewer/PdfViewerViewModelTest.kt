package com.spinel.pdftools.ui.viewer

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

@RunWith(RobolectricTestRunner::class)
class PdfViewerViewModelTest {

    private lateinit var viewModel: PdfViewerViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = PdfViewerViewModel(application)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val state = viewModel.state.first()
        assertTrue(state is PdfViewerState.Loading)
    }

    // A detailed test for parsing content URIs and checking caching requires mocking ContentResolver.
    // This pure logic test just verifies the initial bounds and ViewModel instantiation.
}
