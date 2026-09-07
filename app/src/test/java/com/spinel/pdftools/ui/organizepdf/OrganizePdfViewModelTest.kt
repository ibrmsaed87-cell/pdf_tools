package com.spinel.pdftools.ui.organizepdf

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OrganizePdfViewModelTest {

    private lateinit var viewModel: OrganizePdfViewModel

    @Before
    fun setup() {
        viewModel = OrganizePdfViewModel()
    }

    @Test
    fun testReorderItems() {
        val item1 = PageItem(id = "1", originalIndex = 0)
        val item2 = PageItem(id = "2", originalIndex = 1)
        val item3 = PageItem(id = "3", originalIndex = 2)
        
        viewModel.setItemsForTest(listOf(item1, item2, item3))
        viewModel.reorderItems(0, 2)
        
        val state = viewModel.state.value as OrganizeState.Editing
        assertEquals(3, state.items.size)
        // 2, 3, 1
        assertEquals("2", state.items[0].id)
        assertEquals("3", state.items[1].id)
        assertEquals("1", state.items[2].id)
    }

    @Test
    fun testRotateItem() {
        val item1 = PageItem(id = "1", originalIndex = 0)
        viewModel.setItemsForTest(listOf(item1))
        
        viewModel.rotateItem("1")
        var state = viewModel.state.value as OrganizeState.Editing
        assertEquals(90, state.items[0].rotationDelta)
        
        viewModel.rotateItem("1")
        viewModel.rotateItem("1")
        viewModel.rotateItem("1")
        state = viewModel.state.value as OrganizeState.Editing
        assertEquals(0, state.items[0].rotationDelta) // 360 % 360 == 0
    }

    @Test
    fun testDeleteItem() {
        val item1 = PageItem(id = "1", originalIndex = 0)
        val item2 = PageItem(id = "2", originalIndex = 1)
        viewModel.setItemsForTest(listOf(item1, item2))
        
        viewModel.deleteItem("1")
        val state = viewModel.state.value as OrganizeState.Editing
        assertEquals(1, state.items.size)
        assertEquals("2", state.items[0].id)
    }

    @Test
    fun testCannotDeleteLastItem() {
        val item1 = PageItem(id = "1", originalIndex = 0)
        viewModel.setItemsForTest(listOf(item1))
        
        viewModel.deleteItem("1")
        val state = viewModel.state.value as OrganizeState.Editing
        assertEquals(1, state.items.size)
        assertEquals("1", state.items[0].id)
    }
}

