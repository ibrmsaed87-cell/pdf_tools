import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target1 = "data class Compressing(val progress: Float) : CompressState()"
replacement1 = "data class Compressing(val progress: Float, val message: String) : CompressState()"

target2 = """        viewModelScope.launch {
            _state.value = CompressState.Compressing(0f)
            compressedFile = PdfCompressor.compressPdf(getApplication(), uri, level) { progress ->
                _state.update { CompressState.Compressing(progress) }
            }"""
replacement2 = """        viewModelScope.launch {
            _state.value = CompressState.Compressing(0f, "Preparing...")
            compressedFile = PdfCompressor.compressPdf(getApplication(), uri, level) { progress, msg ->
                _state.value = CompressState.Compressing(progress, msg)
            }"""

target3 = """_state.value = CompressState.Compressing(1f) // Indicates compression done, waiting for save"""
replacement3 = """_state.value = CompressState.Compressing(1f, "Ready to save") // Indicates compression done, waiting for save"""

if target1 in content:
    content = content.replace(target1, replacement1)
if target2 in content:
    content = content.replace(target2, replacement2)
if target3 in content:
    content = content.replace(target3, replacement3)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched ViewModel")
