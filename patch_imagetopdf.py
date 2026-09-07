import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = "is GenerationState.Success -> {"
replacement = """is GenerationState.Success -> {
                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        androidx.compose.runtime.LaunchedEffect(state.generationState) {
                            filesViewModel.onPdfCreated((state.generationState as GenerationState.Success).outputUri)
                        }"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched ImageToPdfScreen")
else:
    print("Target not found in ImageToPdfScreen")
