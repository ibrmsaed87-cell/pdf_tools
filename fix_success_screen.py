import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                onSuccess = {
                    Toast.makeText(context, R.string.msg_pdf_saved, Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },"""

replacement = """                onSuccess = {
                    // Stay on Success screen
                },"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Fixed ScanDocumentScreen.kt")
else:
    print("Target not found!")
