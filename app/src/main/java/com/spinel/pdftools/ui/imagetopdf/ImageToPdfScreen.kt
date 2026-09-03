package com.spinel.pdftools.ui.imagetopdf

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spinel.pdftools.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImageToPdfViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pickMultipleMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(99)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    val createDocument = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.generatePdf(context, uri)
        }
    }

    if (state.isTextEditorVisible) {
        TextEditorDialog(
            onDismiss = { viewModel.hideTextEditor() },
            onSave = { title, body -> viewModel.addTextPage(title, body) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_image_to_pdf),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state.pages.isNotEmpty() && state.generationState !is GenerationState.Success) {
                        IconButton(onClick = { viewModel.showTextEditor() }) {
                            Icon(Icons.Filled.TextSnippet, contentDescription = stringResource(id = R.string.action_add_text))
                        }
                        IconButton(
                            onClick = {
                                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        ) {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = stringResource(id = R.string.action_add_more_images))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                            createDocument.launch("PDF_Tools_$timestamp.pdf")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        enabled = state.generationState is GenerationState.Idle
                    ) {
                        Text(
                            text = stringResource(id = R.string.action_create_pdf),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.generationState is GenerationState.Success -> {
                    val uri = (state.generationState as GenerationState.Success).outputUri
                    SuccessStateView(
                        uri = uri,
                        onDone = { 
                            viewModel.resetState()
                            onNavigateBack()
                        }
                    )
                }
                state.pages.isEmpty() -> {
                    EmptySelectionState(
                        onSelectImages = {
                            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onAddText = {
                            viewModel.showTextEditor()
                        }
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = stringResource(id = R.string.hint_reorder_pages),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ImageSelectionGrid(
                            pages = state.pages,
                            onRemove = { viewModel.removePage(it) },
                            onMove = { from, to -> viewModel.reorderPages(from, to) }
                        )
                    }
                }
            }

            if (state.generationState is GenerationState.Generating) {
                val genState = state.generationState as GenerationState.Generating
                ProcessingOverlay(genState.current, genState.total)
            }
            
            if (state.generationState is GenerationState.Error) {
                val errorKey = (state.generationState as GenerationState.Error).message
                val errorMessage = if (errorKey == "error_no_images") {
                    stringResource(id = R.string.error_no_images)
                } else {
                    stringResource(id = R.string.error_generic)
                }
                
                AlertDialog(
                    onDismissRequest = { viewModel.resetGenerationState() },
                    title = { Text("Error") },
                    text = { Text(errorMessage) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetGenerationState() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TextEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.title_add_text_page)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(id = R.string.hint_title_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(stringResource(id = R.string.hint_body_required)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title.trim(), body.trim()) },
                enabled = body.isNotBlank()
            ) {
                Text(stringResource(id = R.string.action_add_page))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}

@Composable
fun EmptySelectionState(onSelectImages: () -> Unit, onAddText: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(50.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.title_image_to_pdf),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.desc_image_to_pdf_screen),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSelectImages,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.action_select_images),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onAddText,
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Filled.TextSnippet, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.action_add_text),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
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

    fun onDragStart(offset: Offset) {
        gridState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.x.toInt() in item.offset.x..(item.offset.x + item.size.width) &&
                offset.y.toInt() in item.offset.y..(item.offset.y + item.size.height)
            }?.also {
                draggedItemIndex = it.index
            }
    }

    fun onDragInterrupted() {
        draggedItemIndex = null
        draggingItemOffset = Offset.Zero
    }

    fun onDrag(offset: Offset) {
        draggingItemOffset += offset
        val draggedIndex = draggedItemIndex ?: return
        
        val draggedItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggedIndex } ?: return
        val dragCenter = Offset(
            x = draggedItem.offset.x + draggedItem.size.width / 2f + draggingItemOffset.x,
            y = draggedItem.offset.y + draggedItem.size.height / 2f + draggingItemOffset.y
        )

        val targetItem = gridState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            dragCenter.x.toInt() in item.offset.x..(item.offset.x + item.size.width) &&
            dragCenter.y.toInt() in item.offset.y..(item.offset.y + item.size.height)
        }
        
        if (targetItem != null && targetItem.index != draggedIndex) {
            val oldLayoutOffsetX = draggedItem.offset.x
            val oldLayoutOffsetY = draggedItem.offset.y
            val newLayoutOffsetX = targetItem.offset.x
            val newLayoutOffsetY = targetItem.offset.y
            
            onMove(draggedIndex, targetItem.index)
            draggedItemIndex = targetItem.index
            
            draggingItemOffset += Offset(
                (oldLayoutOffsetX - newLayoutOffsetX).toFloat(),
                (oldLayoutOffsetY - newLayoutOffsetY).toFloat()
            )
        }
    }
}

@Composable
fun rememberDragDropGridState(
    gridState: LazyGridState = rememberLazyGridState(),
    onMove: (Int, Int) -> Unit
): DragDropGridState {
    return remember(gridState) { DragDropGridState(gridState, onMove) }
}

@Composable
fun ImageSelectionGrid(pages: List<DocumentPage>, onRemove: (String) -> Unit, onMove: (Int, Int) -> Unit) {
    val dragDropState = rememberDragDropGridState(onMove = onMove)
    
    LazyVerticalGrid(
        state = dragDropState.gridState,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePreviewCard(uri: Uri, pageNumber: Int, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = uri,
                contentDescription = stringResource(id = R.string.content_desc_image_preview),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Page number badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.page_number, pageNumber),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.content_desc_remove_image),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TextPreviewCard(page: DocumentPage.Text, pageNumber: Int, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = stringResource(id = R.string.content_desc_text_preview),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (page.title.isNotBlank()) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Page number badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.page_number, pageNumber),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(id = R.string.content_desc_remove_image),
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProcessingOverlay(current: Int, total: Int) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = R.string.state_creating_pdf, current, total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SuccessStateView(uri: Uri, onDone: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.state_success_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.state_success_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.action_share))
            }
            
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.action_open))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.action_done))
        }
    }
}
