import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """@Composable
fun GenerationOverlay(state.generationState: GenerationState, onDismiss: () -> Unit) {"""
replacement = """@Composable
fun GenerationOverlay(state: GenerationState, onDismiss: () -> Unit) {"""

content = content.replace(target, replacement)

# also fix the launched effect inside GenerationOverlay
target2 = """                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        androidx.compose.runtime.LaunchedEffect(state) {
                            filesViewModel.onPdfCreated((state as GenerationState.Success).outputUri)
                        }"""
replacement2 = """                        val filesViewModel: com.spinel.pdftools.ui.files.FilesViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                        androidx.compose.runtime.LaunchedEffect(state) {
                            filesViewModel.onPdfCreated((state as GenerationState.Success).outputUri)
                        }"""
# wait, inside GenerationOverlay, state IS GenerationState!

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

