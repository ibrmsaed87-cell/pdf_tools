import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = "is ScanState.Success -> {"
replacement = """is ScanState.Success -> {
                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        androidx.compose.runtime.LaunchedEffect(state) {
                            filesViewModel.onPdfCreated((state as ScanState.Success).uri)
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched ScanDocumentScreen")
else:
    print("Target not found in ScanDocumentScreen")
