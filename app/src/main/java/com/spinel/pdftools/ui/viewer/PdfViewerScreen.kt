package com.spinel.pdftools.ui.viewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spinel.pdftools.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    uriString: String,
    onNavigateBack: () -> Unit,
    viewModel: PdfViewerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(uriString) {
        viewModel.loadPdf(uriString)
    }

    var showJumpDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var isFocusMode by rememberSaveable { mutableStateOf(false) }
    
    val view = LocalView.current
    val window = (view.context as? Activity)?.window
    
    LaunchedEffect(isFocusMode, window) {
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, view)
            if (isFocusMode) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }
    
    DisposableEffect(window) {
        onDispose {
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler(enabled = isFocusMode) {
        isFocusMode = false
    }

    Scaffold(
        topBar = {
            if (!isFocusMode) {
                TopAppBar(
                    title = {
                    Text(
                        text = if (state is PdfViewerState.Ready) (state as PdfViewerState.Ready).displayName else stringResource(R.string.title_pdf_viewer),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isFocusMode = true }) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = stringResource(R.string.action_enter_fullscreen))
                    }
                }
            )
            }
        },
        floatingActionButton = {
            if (isFocusMode) {
                SmallFloatingActionButton(
                    onClick = { isFocusMode = false },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Filled.FullscreenExit, contentDescription = stringResource(R.string.action_exit_fullscreen))
                }
            } else if (state is PdfViewerState.Ready) {
                val readyState = state as PdfViewerState.Ready
                val firstVisible = listState.layoutInfo.visibleItemsInfo.maxByOrNull {
                    val itemTop = maxOf(0, it.offset)
                    val itemBottom = minOf(listState.layoutInfo.viewportEndOffset, it.offset + it.size)
                    itemBottom - itemTop
                }
                
                val currentPage = (firstVisible?.index ?: 0) + 1

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { showJumpDialog = true }
                ) {
                    Text(
                        text = stringResource(R.string.msg_page_x_of_y, currentPage, readyState.pageCount),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        floatingActionButtonPosition = if (isFocusMode) FabPosition.End else FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
        ) {
            when (val s = state) {
                is PdfViewerState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.msg_loading_pdf))
                    }
                }
                is PdfViewerState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(s.messageRes),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is PdfViewerState.Ready -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = if (isFocusMode) PaddingValues(0.dp) else PaddingValues(top = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(s.pageCount, key = { it }) { index ->
                            PdfPageViewer(
                                pageIndex = index,
                                viewModel = viewModel,
                                listState = listState
                            )
                        }
                    }

                    if (showJumpDialog) {
                        var pageInput by remember { mutableStateOf("") }
                        var isError by remember { mutableStateOf(false) }

                        AlertDialog(
                            onDismissRequest = { showJumpDialog = false },
                            title = { Text(stringResource(R.string.action_jump_to_page)) },
                            text = {
                                OutlinedTextField(
                                    value = pageInput,
                                    onValueChange = { 
                                        pageInput = it
                                        isError = false
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = isError,
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val page = pageInput.toIntOrNull()
                                    if (page != null && page in 1..s.pageCount) {
                                        coroutineScope.launch {
                                            listState.scrollToItem(page - 1)
                                        }
                                        showJumpDialog = false
                                    } else {
                                        isError = true
                                    }
                                }) {
                                    Text(stringResource(R.string.action_go))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showJumpDialog = false }) {
                                    Text(stringResource(R.string.action_cancel))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PdfPageViewer(
    pageIndex: Int,
    viewModel: PdfViewerViewModel,
    listState: LazyListState
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var aspectRatio by remember { mutableFloatStateOf(0.707f) }
    var isFailed by remember { mutableStateOf(false) }
    
    // Zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val displayWidth = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    LaunchedEffect(pageIndex) {
        aspectRatio = viewModel.getPageAspectRatio(pageIndex)
        bitmap = viewModel.getPageBitmap(pageIndex, displayWidth, isHighRes = false)
        if (bitmap == null) {
            isFailed = true
        }
    }

    // High res trigger when zoomed
    LaunchedEffect(scale) {
        if (scale > 1.5f && bitmap != null && !isFailed) {
            val highRes = viewModel.getPageBitmap(pageIndex, displayWidth, isHighRes = true)
            if (highRes != null) {
                bitmap = highRes
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .aspectRatio(aspectRatio)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        scale = (scale * zoom).coerceIn(1f, 3f)

                        if (scale > 1f) {
                            val maxPanX = (size.width * scale - size.width) / 2
                            val maxPanY = (size.height * scale - size.height) / 2
                            val oldX = offsetX
                            val oldY = offsetY

                            offsetX = (offsetX + pan.x).coerceIn(-maxPanX, maxPanX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxPanY, maxPanY)

                            val consumedY = offsetY - oldY
                            val unconsumedY = pan.y - consumedY

                            if (unconsumedY != 0f) {
                                listState.dispatchRawDelta(-unconsumedY)
                            }

                            if (zoom != 1f || offsetX != oldX || offsetY != oldY || unconsumedY != 0f) {
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                            
                            if (zoom != 1f) {
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth
            )
        } else if (isFailed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.err_page_rendering_failed), color = MaterialTheme.colorScheme.error)
            }
        } else {
            CircularProgressIndicator()
        }
    }
}
