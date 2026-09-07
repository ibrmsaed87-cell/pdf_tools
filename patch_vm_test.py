import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/mergepdf/MergePdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'private val currentItems = mutableListOf<PdfItem>()'
replacement = target + '\n\n    fun addItemsForTest(items: List<PdfItem>) {\n        currentItems.addAll(items)\n        _state.value = MergeState.SelectedFiles(currentItems.toList())\n    }'
content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("VM patched")
