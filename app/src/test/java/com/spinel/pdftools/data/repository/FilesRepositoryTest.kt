package com.spinel.pdftools.data.repository

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.spinel.pdftools.data.model.FileSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FilesRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: FilesRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = FilesRepository(context)
    }

    @Test
    fun testAddCreatedFile() = runBlocking {
        val uri = Uri.parse("file:///tmp/created.pdf")
        repository.addOrUpdateFile(uri, FileSource.CREATED)

        val files = repository.filesFlow.first()
        val match = files.find { it.uri == uri.toString() }
        assertTrue(match != null)
        assertEquals(FileSource.CREATED, match!!.source)
    }

    @Test
    fun testAddOpenedFile() = runBlocking {
        val uri = Uri.parse("file:///tmp/opened.pdf")
        repository.addOrUpdateFile(uri, FileSource.OPENED)

        val files = repository.filesFlow.first()
        val match = files.find { it.uri == uri.toString() }
        assertTrue(match != null)
        assertEquals(FileSource.OPENED, match!!.source)
    }

    @Test
    fun testReopeningSameFileUpdatesLastOpenedAt() = runBlocking {
        val uri = Uri.parse("file:///tmp/test.pdf")
        repository.addOrUpdateFile(uri, FileSource.OPENED)
        val files1 = repository.filesFlow.first()
        val firstOpenedAt = files1.find { it.uri == uri.toString() }!!.lastOpenedAt

        Thread.sleep(10)

        repository.addOrUpdateFile(uri, FileSource.OPENED)
        val files2 = repository.filesFlow.first()
        
        val count = files2.count { it.uri == uri.toString() }
        assertEquals(1, count) // No duplicate
        
        val match = files2.find { it.uri == uri.toString() }!!
        assertTrue(match.lastOpenedAt > firstOpenedAt) // Timestamp updated
    }
}
