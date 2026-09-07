import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }"""

replacement = """                TextButton(onClick = {
                    allowDismiss = true
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }) {
                    Text(stringResource(R.string.action_cancel))
                }"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched Cancel logic")
else:
    print("Target not found")
