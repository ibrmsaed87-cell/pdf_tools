import sys

file_path = 'app/src/main/java/com/spinel/pdftools/ui/viewer/PdfViewerScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

target = '        floatingActionButtonPosition = FabPosition.Center'
replacement = '        floatingActionButtonPosition = if (isFocusMode) FabPosition.End else FabPosition.Center'
content = content.replace(target, replacement)

with open(file_path, 'w') as f:
    f.write(content)
