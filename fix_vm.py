import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/files/FilesViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = "    fun onPdfOpenedFromPicker(uri: Uri) {"
replacement = """    fun onPdfCreated(uri: Uri) {
        viewModelScope.launch {
            repository.addOrUpdateFile(uri, FileSource.CREATED)
        }
    }

    fun onPdfOpenedFromPicker(uri: Uri) {"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched FilesViewModel")
else:
    print("Target not found in FilesViewModel")
