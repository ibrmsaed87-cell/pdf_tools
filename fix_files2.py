import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/files/FilesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """@Composable
fun FilesScreen() {
    var selectedFilter by remember { mutableStateOf(0) }
    val filters = listOf(
        stringResource(id = R.string.filter_recent),
        stringResource(id = R.string.filter_created),
        stringResource(id = R.string.filter_opened)
    )

    // SAF Launcher for PDF (Safe, no broad permissions required)
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            // Ready for future integration: handle selected document URI
        }
    )"""

replacement = """@Composable
fun FilesScreen(viewModel: FilesViewModel = viewModel()) {
    var selectedFilter by remember { mutableIntStateOf(0) }
    val filters = listOf(
        stringResource(id = R.string.filter_recent),
        stringResource(id = R.string.filter_created),
        stringResource(id = R.string.filter_opened)
    )
    
    val allFiles by viewModel.allFiles.collectAsState()
    val context = LocalContext.current

    // SAF Launcher for PDF (Safe, no broad permissions required)
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onPdfOpenedFromPicker(it) }
        }
    )"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched FilesScreen start")
else:
    print("Target FilesScreen start not found")
