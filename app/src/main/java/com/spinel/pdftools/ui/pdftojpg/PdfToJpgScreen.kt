package com.spinel.pdftools.ui.pdftojpg

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.components.PremiumEmptyState
import com.spinel.pdftools.ui.theme.AccentBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToJpgScreen(
    onNavigateBack: () -> Unit,
    viewModel: PdfToJpgViewModel = viewModel()
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
        ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri ->
        if (uri != null) {
            viewModel.saveSingleJpg(context, uri)
        }
    }

    val openDocumentTreeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.saveMultipleJpgs(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_pdf_to_jpg)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is PdfToJpgState.Editing) {
                        val editingState = state as PdfToJpgState.Editing
                        val allSelected = editingState.selectedIndices.size == editingState.items.size && editingState.items.isNotEmpty()
                        
                        TextButton(onClick = {
                            if (allSelected) viewModel.deselectAll() else viewModel.selectAll()
                        }) {
                            Text(if (allSelected) stringResource(R.string.action_deselect_all) else stringResource(R.string.action_select_all))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state is PdfToJpgState.Editing) {
                val editingState = state as PdfToJpgState.Editing
                if (editingState.selectedIndices.isNotEmpty()) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.convertSelectedPages(context) },
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    ) {
                        Text(stringResource(R.string.action_convert_count, editingState.selectedIndices.size))
                    }
                }
            } else if (state is PdfToJpgState.ReadyToSave) {
                val readyState = state as PdfToJpgState.ReadyToSave
                ExtendedFloatingActionButton(
                    onClick = {
                        if (readyState.generatedCount == 1) {
                            val defaultName = readyState.tempDir.listFiles()?.firstOrNull()?.name ?: "document.jpg"
                            createDocumentLauncher.launch(defaultName)
                        } else {
                            openDocumentTreeLauncher.launch(null)
                        }
                    },
                    containerColor = AccentBlue,
                    contentColor = Color.White
                ) {
                    Text(if (readyState.generatedCount == 1) stringResource(R.string.msg_save_image) else stringResource(R.string.msg_save_images))
                }
            } else if (state is PdfToJpgState.Success || state is PdfToJpgState.Error) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.reset() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(stringResource(R.string.action_convert_more))
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = state) {
                is PdfToJpgState.Empty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PremiumEmptyState(
                            title = stringResource(R.string.title_pdf_to_jpg),
                            description = "Select a PDF to convert its pages into high-quality JPG images.",
                            icon = Icons.Filled.CheckCircle
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ExtendedFloatingActionButton(
                            onClick = { openDocumentLauncher.launch(arrayOf("application/pdf")) },
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        ) {
                            Text(stringResource(R.string.action_add_pdf))
                        }
                    }
                }
                is PdfToJpgState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.msg_loading_pages))
                    }
                }
                is PdfToJpgState.Editing -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.items, key = { it.id }) { page ->
                            val isSelected = s.selectedIndices.contains(page.originalIndex)
                            PageThumbnailCard(
                                page = page,
                                isSelected = isSelected,
                                viewModel = viewModel,
                                onToggle = { viewModel.togglePageSelection(page.originalIndex) }
                            )
                        }
                    }
                }
                is PdfToJpgState.Generating -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.msg_converting_pages, s.current, s.total),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(onClick = { viewModel.cancelConversion() }) {
                            Text("Cancel")
                        }
                    }
                }
                is PdfToJpgState.ReadyToSave -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = AccentBlue,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.msg_ready_to_save_images),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${s.generatedCount} images ready",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is PdfToJpgState.Saving -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Saving image ${s.current} of ${s.total}...",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is PdfToJpgState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = AccentBlue,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.msg_images_saved, s.savedCount),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is PdfToJpgState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(s.messageRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageThumbnailCard(
    page: PdfPage,
    isSelected: Boolean,
    viewModel: PdfToJpgViewModel,
    onToggle: () -> Unit
) {
    var thumbnail by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(page.originalIndex) {
        coroutineScope.launch {
            thumbnail = viewModel.getPageThumbnail(page.originalIndex)
        }
    }

    val cardColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f)
            .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail!!.asImageBitmap(),
                    contentDescription = "Page ${page.originalIndex + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
            
            // Checkbox and Page Number
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${page.originalIndex + 1}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
                
                RadioButton(
                    selected = isSelected,
                    onClick = null, // Handled by Card click
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
