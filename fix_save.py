import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                Button(
                    onClick = { onSave(title, body, titleStyle, bodyStyle) },
                    enabled = body.isNotBlank()
                ) {"""

replacement = """                Button(
                    onClick = {
                        allowDismiss = true
                        scope.launch {
                            sheetState.hide()
                            onSave(title, body, titleStyle, bodyStyle)
                        }
                    },
                    enabled = body.isNotBlank()
                ) {"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched Save logic")
else:
    print("Target not found")
