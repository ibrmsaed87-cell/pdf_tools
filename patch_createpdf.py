import re

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
"""

content = content.replace('import java.util.Locale\n', 'import java.util.Locale\n' + imports_to_add)

# Remove burnoutcrew imports
content = re.sub(r'import org\.burnoutcrew\..*\n', '', content)

# Replace PageListView
page_list_view_old = """@Composable
private fun PageListView(
    pages: List<DocumentPage>,
    onRemove: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onEdit: (String) -> Unit
) {
    val state = rememberReorderableLazyListState(
        onMove = { from, to -> onReorder(from.index, to.index) },
        listState = rememberLazyListState()
    )

    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(state)
            .detectReorderAfterLongPress(state),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
            ReorderableItem(state, key = page.id) { isDragging ->
                val modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = if (isDragging) 1.05f else 1f
                        scaleY = if (isDragging) 1.05f else 1f
                        alpha = if (isDragging) 0.8f else 1f
                    }
                    .zIndex(if (isDragging) 1f else 0f)
                
                when (page) {
                    is DocumentPage.Image -> {
                        ImagePreviewCard(
                            uri = page.uri,
                            pageNumber = index + 1,
                            onRemove = { onRemove(page.id) },
                            modifier = modifier
                        )
                    }
                    is DocumentPage.Text -> {
                        TextPreviewCard(
                            page = page,
                            pageNumber = index + 1,
                            onRemove = { onRemove(page.id) },
                            onEdit = { onEdit(page.id) },
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}"""

page_list_view_new = """class DragDropGridState(
    val gridState: LazyGridState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedItemIndex by mutableStateOf<Int?>(null)
        private set
        
    var draggingItemOffset by mutableStateOf(Offset.Zero)
        private set
        
    private var initiallyDraggedElement: LazyGridItemInfo? = null
    private var draggedItemOffset = Offset.Zero
    
    fun onDragStart(offset: Offset) {
        gridState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.x.toInt() in item.offset.x..(item.offset.x + item.size.width) &&
                offset.y.toInt() in item.offset.y..(item.offset.y + item.size.height)
            }?.also {
                initiallyDraggedElement = it
                draggedItemIndex = it.index
            }
    }
    
    fun onDragInterrupted() {
        draggedItemIndex = null
        initiallyDraggedElement = null
        draggingItemOffset = Offset.Zero
        draggedItemOffset = Offset.Zero
    }
    
    fun onDrag(dragAmount: Offset) {
        draggedItemIndex?.let { currentIndex ->
            draggedItemOffset += dragAmount
            draggingItemOffset = draggedItemOffset
            
            val currentElement = initiallyDraggedElement ?: return@let
            val startOffset = Offset(
                x = currentElement.offset.x + draggedItemOffset.x,
                y = currentElement.offset.y + draggedItemOffset.y
            )
            
            val targetItem = gridState.layoutInfo.visibleItemsInfo.find { item ->
                item.index != currentIndex &&
                startOffset.x.toInt() in item.offset.x..(item.offset.x + item.size.width) &&
                startOffset.y.toInt() in item.offset.y..(item.offset.y + item.size.height)
            }
            
            if (targetItem != null) {
                onMove(currentIndex, targetItem.index)
                draggedItemIndex = targetItem.index
                initiallyDraggedElement = targetItem
                draggedItemOffset = Offset.Zero
                draggingItemOffset = Offset.Zero
            }
        }
    }
}

@Composable
fun rememberDragDropGridState(
    gridState: LazyGridState = rememberLazyGridState(),
    onMove: (Int, Int) -> Unit
): DragDropGridState {
    return remember(gridState) {
        DragDropGridState(gridState = gridState, onMove = onMove)
    }
}

@Composable
private fun PageListView(
    pages: List<DocumentPage>,
    onRemove: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onEdit: (String) -> Unit
) {
    val dragDropState = rememberDragDropGridState(onMove = onReorder)
    
    LazyVerticalGrid(
        state = dragDropState.gridState,
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { dragDropState.onDragStart(it) },
                    onDrag = { change, dragAmount -> 
                        change.consume()
                        dragDropState.onDrag(dragAmount) 
                    },
                    onDragEnd = { dragDropState.onDragInterrupted() },
                    onDragCancel = { dragDropState.onDragInterrupted() }
                )
            }
    ) {
        itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
            val isDragging = index == dragDropState.draggedItemIndex
            val offset = if (isDragging) dragDropState.draggingItemOffset else Offset.Zero
            
            val modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offset.x
                    translationY = offset.y
                    scaleX = if (isDragging) 1.05f else 1f
                    scaleY = if (isDragging) 1.05f else 1f
                    alpha = if (isDragging) 0.8f else 1f
                }
                .zIndex(if (isDragging) 1f else 0f)
                
            when (page) {
                is DocumentPage.Image -> {
                    ImagePreviewCard(
                        uri = page.uri,
                        pageNumber = index + 1,
                        onRemove = { onRemove(page.id) },
                        modifier = modifier
                    )
                }
                is DocumentPage.Text -> {
                    TextPreviewCard(
                        page = page,
                        pageNumber = index + 1,
                        onRemove = { onRemove(page.id) },
                        onEdit = { onEdit(page.id) },
                        modifier = modifier
                    )
                }
            }
        }
    }
}"""

content = content.replace(page_list_view_old, page_list_view_new)

# Remove the unused imports
content = content.replace('import androidx.compose.foundation.lazy.LazyColumn\n', '')
content = content.replace('import androidx.compose.foundation.lazy.itemsIndexed\n', '')
content = content.replace('import androidx.compose.foundation.lazy.rememberLazyListState\n', '')

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'w') as f:
    f.write(content)
