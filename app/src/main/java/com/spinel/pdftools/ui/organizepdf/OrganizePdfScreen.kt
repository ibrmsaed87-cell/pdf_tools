package com.spinel.pdftools.ui.organizepdf

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.files.FilesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizePdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: OrganizePdfViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.selectPdf(context, uri)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.savePdf(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_organize_pdf)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state is OrganizeState.Success || state is OrganizeState.ReadyToSave) {
                            viewModel.reset()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is OrganizeState.Editing) {
                        TextButton(onClick = { viewModel.generatePdf(context) }) {
                            Text(
                                text = "Next",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state is OrganizeState.Empty) {
                ExtendedFloatingActionButton(
                    onClick = { openDocumentLauncher.launch(arrayOf("application/pdf")) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.action_add_pdf)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val currentState = state) {
                is OrganizeState.Empty -> {
                    EmptyView()
                }
                is OrganizeState.Loading -> {
                    ProcessingView(stringResource(R.string.msg_loading_pages))
                }
                is OrganizeState.Editing -> {
                    OrganizeList(
                        items = currentState.items,
                        viewModel = viewModel,
                        onReorder = { from, to -> viewModel.reorderItems(from, to) },
                        onDelete = { id -> 
                            if (currentState.items.size > 1) {
                                viewModel.deleteItem(id)
                            } else {
                                Toast.makeText(context, context.getString(R.string.err_cannot_delete_last_page), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRotate = { id -> viewModel.rotateItem(id) }
                    )
                }
                is OrganizeState.Generating -> {
                    ProcessingView(stringResource(R.string.msg_organizing_progress, currentState.current, currentState.total))
                }
                is OrganizeState.ReadyToSave -> {
                    ReadyToSaveView(
                        onSave = {
                            val timeStamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
                            createDocumentLauncher.launch("organized_$timeStamp.pdf")
                        },
                        onCancel = {
                            viewModel.dismissError()
                        }
                    )
                }
                is OrganizeState.Success -> {
                    val filesViewModel: FilesViewModel = viewModel()
                    LaunchedEffect(currentState) {
                        filesViewModel.onPdfCreated(currentState.savedUri)
                    }
                    SuccessView(
                        onDone = {
                            viewModel.reset()
                            onNavigateBack()
                        },
                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(currentState.savedUri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open PDF"))
                        }
                    )
                }
                is OrganizeState.Error -> {
                    val msgResId = context.resources.getIdentifier(currentState.messageRes, "string", context.packageName)
                    val errorMsg = if (msgResId != 0) context.getString(msgResId) else currentState.messageRes
                    
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissError() },
                        title = { Text("Error") },
                        text = { Text(errorMsg) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.title_organize_pdf),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a PDF to rearrange, rotate, or delete pages.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OrganizeList(
    items: List<PageItem>,
    viewModel: OrganizePdfViewModel,
    onReorder: (Int, Int) -> Unit,
    onDelete: (String) -> Unit,
    onRotate: (String) -> Unit
) {
    // Standard reorderable LazyColumn for reliable Drag and Drop on Android
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            var isDragging by remember { mutableStateOf(false) }
            var offsetY by remember { mutableStateOf(0f) }
            val draggedElevation = if (isDragging) 8.dp else 2.dp
            
            val currentViewIndex by rememberUpdatedState(index)
            val totalItems by rememberUpdatedState(items.size)
            
            val density = LocalDensity.current
            val itemHeightPx = remember(density) { with(density) { 208.dp.toPx() } }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .zIndex(if (isDragging) 1f else 0f)
                    .shadow(draggedElevation, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Drag Handle & Index
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = { 
                                        isDragging = false
                                        offsetY = 0f
                                    },
                                    onDragCancel = { 
                                        isDragging = false
                                        offsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y
                                        
                                        val cIndex = currentViewIndex
                                        if (offsetY > itemHeightPx && cIndex < totalItems - 1) {
                                            onReorder(cIndex, cIndex + 1)
                                            offsetY -= itemHeightPx
                                        } else if (offsetY < -itemHeightPx && cIndex > 0) {
                                            onReorder(cIndex, cIndex - 1)
                                            offsetY += itemHeightPx
                                        }
                                    }
                                )
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            Icons.Filled.DragHandle,
                            contentDescription = "Reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
                        
                        LaunchedEffect(item.originalIndex) {
                            thumbnail = viewModel.getThumbnail(item.originalIndex)
                        }
                        
                        if (thumbnail != null) {
                            Image(
                                bitmap = thumbnail!!.asImageBitmap(),
                                contentDescription = "Page ${index + 1}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(item.rotationDelta.toFloat())
                            )
                        } else {
                            CircularProgressIndicator()
                        }
                    }

                    // Actions
                    Column(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { onRotate(item.id) }) {
                            Icon(
                                Icons.Filled.RotateRight,
                                contentDescription = stringResource(R.string.action_rotate),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onDelete(item.id) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun ReadyToSaveView(onSave: () -> Unit, onCancel: () -> Unit) {
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
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_ready_to_save_organized),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_cancel))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_save_organized))
            }
        }
    }
}

@Composable
fun SuccessView(onDone: () -> Unit, onOpen: () -> Unit) {
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
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Success",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onOpen,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open PDF")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}
