package com.spinel.pdftools.ui.scandocument

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.spinel.pdftools.R
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDocumentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToViewer: (String) -> Unit = {},
    viewModel: ScanDocumentViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val pages by viewModel.pages.collectAsState()
    
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { destUri ->
            viewModel.generatePdf(
                context = context,
                destUri = destUri,
                onSuccess = {
                    // Stay on Success screen
                },
                onError = {
                    Toast.makeText(context, R.string.err_pdf_generation_failed, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_scan_document)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state is ScanState.Camera && pages.isNotEmpty()) {
                            viewModel.cancelCamera()
                        } else if (state !is ScanState.ViewingDocument && state !is ScanState.Camera) {
                            viewModel.retakePhoto()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!hasCameraPermission) {
                PermissionRationale(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            } else {
                when (val scanState = state) {
                    is ScanState.Camera -> {
                        BackHandler {
                            if (pages.isNotEmpty()) {
                                viewModel.cancelCamera()
                            } else {
                                onNavigateBack()
                            }
                        }
                        CameraView(
                            onPhotoCaptured = { capture, ctx, executor ->
                                viewModel.capturePhoto(capture, ctx, executor)
                            }
                        )
                    }
                    is ScanState.Preview -> {
                        BackHandler { viewModel.retakePhoto() }
                        ImagePreviewView(
                            uri = scanState.uri,
                            onRetake = { viewModel.retakePhoto() },
                            onAccept = { viewModel.startCrop() }
                        )
                    }
                    is ScanState.Cropping -> {
                        BackHandler { viewModel.retakePhoto() }
                        CroppingView(
                            uri = scanState.uri,
                            initialQuad = scanState.initialQuad,
                            onRetake = { viewModel.retakePhoto() },
                            onApplyCrop = { quad ->
                                if (CropGeometry.isValidQuadrilateral(quad)) {
                                    viewModel.applyCrop(context, scanState.uri, quad)
                                } else {
                                    Toast.makeText(context, R.string.msg_invalid_crop, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    is ScanState.CorrectedPreview -> {
                        BackHandler { viewModel.editCrop() }
                        CorrectedPreviewView(
                            correctedUri = scanState.correctedUri,
                            onEditCrop = { viewModel.editCrop() },
                            onUseScan = { viewModel.acceptScan() }
                        )
                    }
                    is ScanState.ViewingDocument -> {
                        BackHandler { onNavigateBack() }
                        ViewingDocumentView(
                            pages = pages,
                            onAddPage = { viewModel.requestAddPage() },
                            onRemovePage = { id -> viewModel.removePage(id) },
                            onReorder = { from, to -> viewModel.reorderPages(from, to) },
                            onSave = {
                                if (pages.isNotEmpty()) {
                                    createDocumentLauncher.launch(viewModel.getDefaultFileName())
                                } else {
                                    Toast.makeText(context, R.string.err_no_pages_to_save, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                    is ScanState.SavingPdf -> {
                        BackHandler { /* Disable back during save */ }
                        SavingPdfView(progress = scanState.progress)
                    }
                    is ScanState.Success -> {
                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        androidx.compose.runtime.LaunchedEffect(scanState) {
                            filesViewModel.onPdfCreated(scanState.uri)
                        }
                        BackHandler { onNavigateBack() }
                        ScanSuccessView(
                            uri = scanState.uri,
                            onDone = { onNavigateBack() },
                            onNavigateToViewer = onNavigateToViewer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewingDocumentView(
    pages: List<ScannedPage>,
    onAddPage: () -> Unit,
    onRemovePage: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (pages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(R.string.msg_no_pages), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                ScanMixedSelectionGrid(
                    pages = pages,
                    onRemove = onRemovePage,
                    onReorder = onReorder
                )
            }
        }
        
        BottomAppBar(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onAddPage) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_add_page))
                }
                
                Button(onClick = onSave, enabled = pages.isNotEmpty()) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_save_pdf))
                }
            }
        }
    }
}

@Composable
private fun ScanMixedSelectionGrid(
    pages: List<ScannedPage>,
    onRemove: (String) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    val dragDropState = rememberScanDragDropGridState(onMove = onReorder)
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
                
            ScanPagePreviewCard(
                uri = page.uri,
                pageNumber = index + 1,
                onRemove = { onRemove(page.id) },
                modifier = modifier
            )
        }
    }
}

@Composable
fun ScanPagePreviewCard(
    uri: Uri,
    pageNumber: Int,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = uri,
                contentDescription = "Page $pageNumber",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = pageNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.msg_delete_page),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SavingPdfView(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(64.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.msg_generating_pdf),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

class ScanDragDropGridState(
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
fun rememberScanDragDropGridState(
    gridState: LazyGridState = rememberLazyGridState(),
    onMove: (Int, Int) -> Unit
): ScanDragDropGridState {
    return remember(gridState) {
        ScanDragDropGridState(gridState = gridState, onMove = onMove)
    }
}

// Below are the unchanged functions from original file

@Composable
fun PermissionRationale(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.permission_camera_rationale),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.action_grant_permission))
        }
    }
}

@Composable
fun CameraView(
    onPhotoCaptured: (ImageCapture, Context, java.util.concurrent.Executor) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            FloatingActionButton(
                onClick = { onPhotoCaptured(imageCapture, context, cameraExecutor) },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun ImagePreviewView(
    uri: Uri,
    onRetake: () -> Unit,
    onAccept: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = uri,
                contentDescription = "Captured Document",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onRetake) {
                Text(
                    text = stringResource(R.string.action_retake),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.action_use_photo),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun CorrectedPreviewView(
    correctedUri: Uri,
    onEditCrop: () -> Unit,
    onUseScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = correctedUri,
                contentDescription = "Corrected Document",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onEditCrop) {
                Text(
                    text = stringResource(R.string.action_edit_crop),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Button(
                onClick = onUseScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.action_use_scan),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun ScanSuccessView(
    uri: Uri,
    onDone: () -> Unit,
    onNavigateToViewer: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_pdf_saved),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_done))
            }
            Button(
                onClick = { onNavigateToViewer(uri.toString()) },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_open))
            }
        }
    }
}
