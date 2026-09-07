import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = "package com.spinel.pdftools.ui.imagetopdf"
replacement = "package com.spinel.pdftools.ui.imagetopdf\n\nimport kotlinx.coroutines.launch"

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched import")
else:
    print("Target not found")
