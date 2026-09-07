package com.spinel.pdftools.ui.splitpdf

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spinel.pdftools.R
import com.spinel.pdftools.ui.files.FilesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitPdfScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitPdfViewModel = viewModel()
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
            viewModel.extractPdf(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_split_pdf)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when (val currentState = state) {
                is SplitState.Empty -> {
                    EmptyStateView(
                        onSelectFile = {
                            openDocumentLauncher.launch(arrayOf("application/pdf"))
                        }
                    )
                }
                is SplitState.Selected -> {
                    SelectedView(
                        state = currentState,
                        onChangeFile = {
                            openDocumentLauncher.launch(arrayOf("application/pdf"))
                        },
                        onModeSelected = { viewModel.setSelectionMode(it) },
                        onCustomRangeChanged = { viewModel.setCustomRange(it) },
                        onExtract = {
                            val originalName = currentState.item.name
                            val newName = if (originalName.lowercase().endsWith(".pdf")) {
                                originalName.substring(0, originalName.length - 4) + "_extracted.pdf"
                            } else {
                                originalName + "_extracted.pdf"
                            }
                            createDocumentLauncher.launch(newName)
                        }
                    )
                }
                is SplitState.Processing -> {
                    ProcessingView()
                }
                is SplitState.Success -> {
                    val filesViewModel: FilesViewModel = viewModel()
                    LaunchedEffect(currentState) {
                        filesViewModel.onPdfCreated(currentState.savedUri)
                    }
                    SuccessView(
                        onDone = {
                            viewModel.reset()
                        },
                        onOpen = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(currentState.savedUri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(intent, "Open PDF"))
                            } catch (e: Exception) {
                                // No app found
                            }
                            viewModel.reset()
                        }
                    )
                }
                is SplitState.Error -> {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissError() },
                        title = { Text("Error") },
                        text = {
                            val msgRes = if (currentState.message == "err_unable_to_read_pdf") {
                                stringResource(R.string.err_unable_to_read_pdf, currentState.arg)
                            } else if (currentState.message == "err_extraction_failed") {
                                stringResource(R.string.err_unable_to_merge_pdf) // fallback to generic or we can add specific string
                            } else {
                                stringResource(R.string.err_unable_to_merge_pdf)
                            }
                            Text(msgRes)
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissError() }) {
                                Text(stringResource(R.string.action_done))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(onSelectFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCut,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.title_split_pdf),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.desc_split_pdf),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onSelectFile,
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.action_select_pdf))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedView(
    state: SplitState.Selected,
    onChangeFile: () -> Unit,
    onModeSelected: (SelectionMode) -> Unit,
    onCustomRangeChanged: (String) -> Unit,
    onExtract: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // PDF File Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp)
                .background(MaterialTheme.colorScheme.surface),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.PictureAsPdf,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.item.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.msg_total_pages, state.item.pageCount) + " • ${state.item.sizeStr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = onChangeFile, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.action_change_pdf))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selection Options
        Text(
            text = stringResource(R.string.msg_selection_mode),
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.mode == SelectionMode.ALL_PAGES,
                onClick = { onModeSelected(SelectionMode.ALL_PAGES) }
            )
            Text(
                text = stringResource(R.string.mode_all_pages),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.mode == SelectionMode.CUSTOM_PAGES,
                onClick = { onModeSelected(SelectionMode.CUSTOM_PAGES) }
            )
            Text(
                text = stringResource(R.string.mode_custom_pages),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (state.mode == SelectionMode.CUSTOM_PAGES) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.customRange,
                onValueChange = onCustomRangeChanged,
                label = { Text(stringResource(R.string.label_page_range)) },
                placeholder = { Text("e.g. 1-3, 5, 8") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.validationError != null,
                supportingText = {
                    if (state.validationError != null) {
                        val errMsg = when(state.validationError) {
                            "err_at_least_one_page" -> stringResource(R.string.err_at_least_one_page)
                            "err_page_does_not_exist" -> stringResource(R.string.err_page_does_not_exist)
                            else -> stringResource(R.string.err_invalid_page_range)
                        }
                        Text(errMsg)
                    } else {
                        Text(stringResource(R.string.msg_page_range_hint))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onExtract,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.ContentCut, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.action_extract_pages),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ProcessingView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_extracting_pages),
            style = MaterialTheme.typography.titleMedium
        )
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_pdf_saved_successfully),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_done))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onOpen, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_open))
            }
        }
    }
}
