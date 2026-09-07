import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_state = """    object ViewingDocument : ScanState()
    data class SavingPdf(val progress: Float) : ScanState()
}"""
replacement_state = """    object ViewingDocument : ScanState()
    data class SavingPdf(val progress: Float) : ScanState()
    data class Success(val uri: Uri) : ScanState()
}"""
content = content.replace(target_state, replacement_state)

target_retake = """        if (_pages.value.isNotEmpty()) {
            _state.value = ScanState.ViewingDocument
        } else {
            _state.value = ScanState.Camera
        }"""
replacement_retake = """        _state.value = ScanState.Camera"""
content = content.replace(target_retake, replacement_retake)

target_generate = """                launch(Dispatchers.Main) {
                    onSuccess()
                    reset()
                }"""
replacement_generate = """                launch(Dispatchers.Main) {
                    _state.value = ScanState.Success(destUri)
                    onSuccess()
                }"""
content = content.replace(target_generate, replacement_generate)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("ViewModel patched")
