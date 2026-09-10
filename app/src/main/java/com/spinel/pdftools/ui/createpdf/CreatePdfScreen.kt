package com.spinel.pdftools.ui.createpdf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.imagetopdf.DocumentPage
import com.spinel.pdftools.ui.imagetopdf.GenerationState
import com.spinel.pdftools.ui.imagetopdf.TextAlignment
import com.spinel.pdftools.ui.imagetopdf.TextColor
import com.spinel.pdftools.ui.imagetopdf.TextStyleConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePdfScreen(
    onNavigateBack: () -> Unit,
    onNavigateToViewer: (String) -> Unit,
    viewModel: CreatePdfViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.generatePdf(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_create_pdf)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.pages.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                                val filename = "document_${sdf.format(Date())}.pdf"
                                createDocumentLauncher.launch(filename)
                            }
                        ) {
                            Text(stringResource(R.string.action_create_pdf))
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { viewModel.showTextEditor() }) {
                        Icon(Icons.Default.TextFields, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_add_text))
                    }
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_add_images))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (state.pages.isEmpty()) {
                EmptyStateView(
                    onAddTextClick = { viewModel.showTextEditor() },
                    onAddImagesClick = { imagePickerLauncher.launch("image/*") }
                )
            } else {
                PageListView(
                    pages = state.pages,
                    onRemove = { viewModel.removePage(it) },
                    onReorder = { from, to -> viewModel.reorderPages(from, to) },
                    onEdit = { pageId -> viewModel.showTextEditor(pageId) }
                )
            }

            if (state.generationState !is GenerationState.Idle) {
                GenerationOverlay(
                    state = state.generationState,
                    onDismiss = { viewModel.resetGenerationState() },
                    onDone = {
                        viewModel.resetGenerationState()
                        viewModel.resetState()
                        onNavigateBack()
                    },
                    onViewPdf = { uri ->
                        viewModel.resetGenerationState()
                        viewModel.resetState()
                        onNavigateToViewer(uri.toString())
                    }
                )
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
private fun EmptyStateView(onAddTextClick: () -> Unit, onAddImagesClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.DocumentScanner,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_empty_document),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedButton(onClick = onAddTextClick) {
                Icon(Icons.Default.TextFields, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_add_text))
            }
            ElevatedButton(onClick = onAddImagesClick) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_add_images))
            }
        }
    }
}

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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Page $pageNumber",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = pageNumber.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (page.title.isNotBlank()) {
                    Text(
                        text = page.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (page.titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontSize = page.titleStyle.fontSize.sp,
                        textAlign = when(page.titleStyle.alignment) {
                            TextAlignment.Left -> TextAlign.Left
                            TextAlignment.Center -> TextAlign.Center
                            TextAlignment.Right -> TextAlign.Right
                        },
                        color = Color(page.titleStyle.color.colorValue)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (page.body.isNotBlank()) {
                    Text(
                        text = page.body,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (page.bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontSize = page.bodyStyle.fontSize.sp,
                        textAlign = when(page.bodyStyle.alignment) {
                            TextAlignment.Left -> TextAlign.Left
                            TextAlignment.Center -> TextAlign.Center
                            TextAlignment.Right -> TextAlign.Right
                        },
                        color = Color(page.bodyStyle.color.colorValue)
                    )
                }
            }
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = pageNumber.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
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
                Spacer(modifier = Modifier.width(8.dp))
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
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val defaultAlignment = if (layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl) TextAlignment.Right else TextAlignment.Left

    var title by remember { mutableStateOf(editingPage?.title ?: "") }
    var body by remember { mutableStateOf(editingPage?.body ?: "") }
    
    var titleStyle by remember { mutableStateOf(editingPage?.titleStyle ?: TextStyleConfig(fontSize = 28, isBold = true, alignment = defaultAlignment)) }
    var bodyStyle by remember { mutableStateOf(editingPage?.bodyStyle ?: TextStyleConfig(alignment = defaultAlignment)) }
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Title, 1 = Body
    var allowDismiss by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { if (it == androidx.compose.material3.SheetValue.Hidden) allowDismiss else true }
    )
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            if (allowDismiss) onDismiss()
        },
        sheetState = sheetState,
        properties = androidx.compose.material3.ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        )
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
        val isImeVisible = imeBottom > 0
        
        BackHandler {
            if (isImeVisible) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                allowDismiss = true
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            Text(
                text = stringResource(if (editingPage == null) R.string.action_add_text else R.string.action_edit_text),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text(stringResource(R.string.text_editor_title), modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text(stringResource(R.string.text_editor_body), modifier = Modifier.padding(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Format Toolbar
            val currentStyle = if (selectedTab == 0) titleStyle else bodyStyle
            val updateStyle: (TextStyleConfig) -> Unit = { 
                if (selectedTab == 0) titleStyle = it else bodyStyle = it 
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        IconButton(onClick = { updateStyle(currentStyle.copy(alignment = TextAlignment.Left)) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.FormatAlignLeft, 
                                contentDescription = "Align Left",
                                tint = if (currentStyle.alignment == TextAlignment.Left) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { updateStyle(currentStyle.copy(alignment = TextAlignment.Center)) }) {
                            Icon(
                                Icons.Default.FormatAlignCenter, 
                                contentDescription = "Align Center",
                                tint = if (currentStyle.alignment == TextAlignment.Center) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { updateStyle(currentStyle.copy(alignment = TextAlignment.Right)) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.FormatAlignRight, 
                                contentDescription = "Align Right",
                                tint = if (currentStyle.alignment == TextAlignment.Right) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Size
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    IconButton(onClick = { updateStyle(currentStyle.copy(fontSize = maxOf(10, currentStyle.fontSize - 2))) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease size")
                    }
                    Text("${currentStyle.fontSize}pt", modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { updateStyle(currentStyle.copy(fontSize = minOf(72, currentStyle.fontSize + 2))) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase size")
                    }
                }
                
                // Bold
                IconButton(
                    onClick = { updateStyle(currentStyle.copy(isBold = !currentStyle.isBold)) },
                    modifier = Modifier.background(
                        if (currentStyle.isBold) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, 
                        RoundedCornerShape(8.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.FormatBold, 
                        contentDescription = "Bold",
                        tint = if (currentStyle.isBold) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextColor.values().forEach { c ->
                    val isSelected = currentStyle.color == c
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(c.colorValue), CircleShape)
                            .clickable { updateStyle(currentStyle.copy(color = c)) }
                            .then(
                                if (isSelected) Modifier.padding(2.dp).background(Color(c.colorValue), CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = if (c == TextColor.White) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            val isDark = androidx.compose.foundation.isSystemInDarkTheme()

            // Fields
            if (selectedTab == 0) {
                val titleAlign = when (titleStyle.alignment) {
                    TextAlignment.Left -> TextAlign.Left
                    TextAlignment.Center -> TextAlign.Center
                    TextAlignment.Right -> TextAlign.Right
                }
                val needsLightBgTitle = isDark && (titleStyle.color == TextColor.Black || titleStyle.color == TextColor.DarkGray)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it 
                        isError = false
                    },
                    label = { Text(stringResource(R.string.text_editor_title_optional)) },
                    modifier = Modifier.fillMaxWidth(),
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
            } else {
                val bodyAlign = when (bodyStyle.alignment) {
                    TextAlignment.Left -> TextAlign.Left
                    TextAlignment.Center -> TextAlign.Center
                    TextAlignment.Right -> TextAlign.Right
                }
                val needsLightBgBody = isDark && (bodyStyle.color == TextColor.Black || bodyStyle.color == TextColor.DarkGray)
                
                OutlinedTextField(
                    value = body,
                    onValueChange = { 
                        body = it 
                        isError = false
                    },
                    label = { Text(stringResource(R.string.text_editor_body)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
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
            }
            
            if (isError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.err_blank_text_page),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
                        
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    allowDismiss = true
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }) {
                    Text(stringResource(R.string.action_cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (title.isBlank() && body.isBlank()) {
                            isError = true
                        } else {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            allowDismiss = true
                            scope.launch {
                                sheetState.hide()
                                onSave(title, body, titleStyle, bodyStyle)
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GenerationOverlay(
    state: GenerationState, 
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    onViewPdf: (Uri) -> Unit
) {
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
                            text = stringResource(R.string.msg_creating_pdf),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (state.total > 0) state.current.toFloat() / state.total else 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is GenerationState.Success -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = viewModel()
                        LaunchedEffect(state) {
                            filesViewModel.onPdfCreated(state.outputUri)
                            
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PDF Created",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { com.spinel.pdftools.monetization.InterstitialAdManager.showInterstitialIfEligible(context as android.app.Activity) { onDone() } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_done))
                            }
                            Button(
                                onClick = { com.spinel.pdftools.monetization.InterstitialAdManager.showInterstitialIfEligible(context as android.app.Activity) { onViewPdf(state.outputUri) } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_view_pdf))
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
