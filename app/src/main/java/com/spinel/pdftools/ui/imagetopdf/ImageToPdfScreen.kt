package com.spinel.pdftools.ui.imagetopdf

import androidx.compose.material.icons.filled.CheckCircle
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spinel.pdftools.R

class DragDropGridState(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImageToPdfViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    val savePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.generatePdf(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_image_to_pdf)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                    }
                },
                actions = {
                    if (state.pages.isNotEmpty() && state.generationState !is GenerationState.Success) {
                        IconButton(onClick = { viewModel.showTextEditor() }) {
                            Icon(Icons.AutoMirrored.Filled.TextSnippet, contentDescription = "Add Text")
                        }
                        IconButton(onClick = { imagePicker.launch("image/*") }) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = stringResource(R.string.action_add_images))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.pages.isNotEmpty() && state.generationState !is GenerationState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            val timestamp = System.currentTimeMillis()
                            savePdfPicker.launch("PDF_Tools_$timestamp.pdf")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = state.generationState is GenerationState.Idle
                    ) {
                        Text(
                            text = stringResource(R.string.action_create_pdf),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.pages.isEmpty()) {
                EmptyStateView(onAddClick = { imagePicker.launch("image/*") })
            } else {
                MixedSelectionGrid(
                    pages = state.pages,
                    onRemove = { viewModel.removePage(it) },
                    onReorder = { from, to -> viewModel.reorderPages(from, to) },
                    onEdit = { pageId -> viewModel.showTextEditor(pageId) }
                )
            }

            if (state.generationState !is GenerationState.Idle) {
                GenerationOverlay(state.generationState) {
                    viewModel.resetGenerationState()
                }
            }
        }
        
        if (state.isTextEditorVisible) {
            val editingPage = state.pages.find { it.id == state.editingTextPageId } as? DocumentPage.Text
            TextEditorBottomSheet(
                editingPage = editingPage,
                onDismiss = { viewModel.hideTextEditor() },
                onSave = { t, b, tStyle, bStyle -> 
                    viewModel.saveTextPage(t, b, tStyle, bStyle) 
                }
            )
        }
    }
}

@Composable
private fun EmptyStateView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clickable { onAddClick() },
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_no_images_selected),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MixedSelectionGrid(
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
}

@Composable
private fun ImagePreviewCard(
    uri: Uri,
    pageNumber: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = uri,
                contentDescription = "Image preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Badge(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Text(pageNumber.toString())
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TextPreviewCard(
    page: DocumentPage.Text,
    pageNumber: Int,
    onRemove: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (page.title.isNotBlank()) {
                    val titleAlign = when (page.titleStyle.alignment) {
                        TextAlignment.Start -> TextAlign.Start
                        TextAlignment.Center -> TextAlign.Center
                        TextAlignment.End -> TextAlign.End
                    }
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = (page.titleStyle.fontSize / 1.5).sp,
                        fontWeight = if (page.titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                        color = Color(page.titleStyle.color.colorValue),
                        textAlign = titleAlign,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                val bodyAlign = when (page.bodyStyle.alignment) {
                    TextAlignment.Start -> TextAlign.Start
                    TextAlignment.Center -> TextAlign.Center
                    TextAlignment.End -> TextAlign.End
                }
                Text(
                    text = page.body.ifBlank { "..." },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = (page.bodyStyle.fontSize / 1.5).sp,
                    fontWeight = if (page.bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(page.bodyStyle.color.colorValue),
                    textAlign = bodyAlign,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Badge(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Text(pageNumber.toString())
            }

            Column(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextEditorBottomSheet(
    editingPage: DocumentPage.Text?,
    onDismiss: () -> Unit,
    onSave: (String, String, TextStyleConfig, TextStyleConfig) -> Unit
) {
    var title by remember { mutableStateOf(editingPage?.title ?: "") }
    var body by remember { mutableStateOf(editingPage?.body ?: "") }
    
    var titleStyle by remember { mutableStateOf(editingPage?.titleStyle ?: TextStyleConfig(fontSize = 28, isBold = true)) }
    var bodyStyle by remember { mutableStateOf(editingPage?.bodyStyle ?: TextStyleConfig()) }
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Title, 1 = Body

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            Text(
                text = if (editingPage == null) stringResource(R.string.title_add_text_page) else stringResource(R.string.action_edit),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.tab_title)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.tab_body)) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val activeStyle = if (selectedTab == 0) titleStyle else bodyStyle


            // Formatting Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    val scaleX = if (isRtl) -1f else 1f

                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Start,
                        onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.Start) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.Start) }
                    ) {
                        Icon(Icons.Filled.FormatAlignLeft, contentDescription = stringResource(R.string.content_desc_align_start), modifier = Modifier.scale(scaleX))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Center,
                        onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.Center) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.Center) }
                    ) {
                        Icon(Icons.Filled.FormatAlignCenter, contentDescription = stringResource(R.string.content_desc_align_center))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.End,
                        onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.End) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.End) }
                    ) {
                        Icon(Icons.Filled.FormatAlignRight, contentDescription = stringResource(R.string.content_desc_align_end), modifier = Modifier.scale(scaleX))
                    }
                }

                // Bold
                IconToggleButton(
                    checked = activeStyle.isBold,
                    onCheckedChange = { isBold -> if (selectedTab == 0) titleStyle = titleStyle.copy(isBold = isBold) else bodyStyle = bodyStyle.copy(isBold = isBold) }
                ) {
                    Icon(Icons.Filled.FormatBold, contentDescription = stringResource(R.string.content_desc_bold))
                }
                
                // Font Size
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (selectedTab == 0) { if (titleStyle.fontSize > 12) titleStyle = titleStyle.copy(fontSize = titleStyle.fontSize - 1) } else { if (bodyStyle.fontSize > 12) bodyStyle = bodyStyle.copy(fontSize = bodyStyle.fontSize - 1) } }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }
                    Text("${activeStyle.fontSize}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { if (selectedTab == 0) { if (titleStyle.fontSize < 40) titleStyle = titleStyle.copy(fontSize = titleStyle.fontSize + 1) } else { if (bodyStyle.fontSize < 40) bodyStyle = bodyStyle.copy(fontSize = bodyStyle.fontSize + 1) } }) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Color Palette
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TextColor.values()) { tColor ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(tColor.colorValue))
                            .border(
                                width = if (activeStyle.color == tColor) 3.dp else 1.dp,
                                color = if (activeStyle.color == tColor) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { if (selectedTab == 0) titleStyle = titleStyle.copy(color = tColor) else bodyStyle = bodyStyle.copy(color = tColor) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TITLE FIELD
            val titleAlign = when (titleStyle.alignment) {
                TextAlignment.Start -> TextAlign.Start
                TextAlignment.Center -> TextAlign.Center
                TextAlignment.End -> TextAlign.End
            }
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val needsLightBgTitle = isDark && (titleStyle.color == TextColor.Black || titleStyle.color == TextColor.DarkGray)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.hint_title_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(titleStyle.color.colorValue),
                    unfocusedTextColor = Color(titleStyle.color.colorValue),
                    focusedContainerColor = if (needsLightBgTitle) Color(0xFFF5F5F5) else Color.Transparent,
                    unfocusedContainerColor = if (needsLightBgTitle) Color(0xFFF5F5F5) else Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // BODY FIELD
            val bodyAlign = when (bodyStyle.alignment) {
                TextAlignment.Start -> TextAlign.Start
                TextAlignment.Center -> TextAlign.Center
                TextAlignment.End -> TextAlign.End
            }
            val needsLightBgBody = isDark && (bodyStyle.color == TextColor.Black || bodyStyle.color == TextColor.DarkGray)
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.hint_body_required)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(min = 100.dp, max = 200.dp),
                minLines = 4,
                maxLines = 10,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(bodyStyle.color.colorValue),
                    unfocusedTextColor = Color(bodyStyle.color.colorValue),
                    focusedContainerColor = if (needsLightBgBody) Color(0xFFF5F5F5) else Color.Transparent,
                    unfocusedContainerColor = if (needsLightBgBody) Color(0xFFF5F5F5) else Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSave(title, body, titleStyle, bodyStyle) },
                    enabled = body.isNotBlank()
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GenerationOverlay(state: GenerationState, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is GenerationState.Generating -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Generating PDF...",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (state.total > 0) state.current.toFloat() / state.total else 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is GenerationState.Success -> {
                        val context = LocalContext.current
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.msg_pdf_saved),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_done))
                            }
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(state.outputUri, "application/pdf")
                                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_open))
                            }
                        }
                    }
                    is GenerationState.Error -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error generating PDF",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("OK")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
