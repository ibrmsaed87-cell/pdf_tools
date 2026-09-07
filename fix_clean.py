import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                        androidx.compose.runtime.LaunchedEffect(state) {
                            filesViewModel.onPdfCreated((state as GenerationState.Success).outputUri)
                        }"""
replacement = """                        androidx.compose.runtime.LaunchedEffect(state.generationState) {
                            filesViewModel.onPdfCreated(state.generationState.outputUri)
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                        androidx.compose.runtime.LaunchedEffect(state) {
                            filesViewModel.onPdfCreated((state as ScanState.Success).uri)
                        }"""
replacement = """                        androidx.compose.runtime.LaunchedEffect(scanState) {
                            filesViewModel.onPdfCreated(scanState.uri)
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

