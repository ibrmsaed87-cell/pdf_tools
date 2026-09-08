import sys

file_path = 'app/src/main/java/com/spinel/pdftools/ui/viewer/PdfViewerScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

# 1. Add necessary imports
imports = """import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity"""

if "import androidx.activity.compose.BackHandler" not in content:
    content = content.replace('import androidx.compose.runtime.*', 'import androidx.compose.runtime.*\n' + imports)

# 2. Add isFocusMode and System Bars handling
setup_code = """    var showJumpDialog by remember { mutableStateOf(false) }
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
    }"""

content = content.replace("""    var showJumpDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()""", setup_code)

# 3. Update topBar logic
topbar_target = """        topBar = {
            TopAppBar(
                title = {"""
topbar_replacement = """        topBar = {
            if (!isFocusMode) {
                TopAppBar(
                    title = {"""

content = content.replace(topbar_target, topbar_replacement)

# Close the if(!isFocusMode) block for TopAppBar
# Let's find the end of the TopAppBar block
# The original code has:
#                 navigationIcon = {
#                     IconButton(onClick = onNavigateBack) {
#                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
#                     }
#                 }
#             )
#         },

topbar_actions = """                navigationIcon = {
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
        },"""

content = content.replace("""                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },""", topbar_actions)

# 4. Update floatingActionButton logic for Page X/Y and Exit Fullscreen
fab_target = """        floatingActionButton = {
            if (state is PdfViewerState.Ready) {"""
fab_replacement = """        floatingActionButton = {
            if (isFocusMode) {
                SmallFloatingActionButton(
                    onClick = { isFocusMode = false },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    Icon(Icons.Filled.FullscreenExit, contentDescription = stringResource(R.string.action_exit_fullscreen))
                }
            } else if (state is PdfViewerState.Ready) {"""

content = content.replace(fab_target, fab_replacement)

# 5. Adjust padding for LazyColumn in Focus Mode
lazy_padding_target = """                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),"""
lazy_padding_replacement = """                        contentPadding = if (isFocusMode) PaddingValues(0.dp) else PaddingValues(top = 16.dp, bottom = 80.dp),"""
content = content.replace(lazy_padding_target, lazy_padding_replacement)

with open(file_path, 'w') as f:
    f.write(content)
