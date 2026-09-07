import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                        CroppingView(
                            uri = scanState.uri,
                            initialQuad = scanState.initialQuad,
                            onCancel = { viewModel.retakePhoto() },
                            onCrop = { quad ->"""

replacement = """                        CroppingView(
                            uri = scanState.uri,
                            initialQuad = scanState.initialQuad,
                            onRetake = { viewModel.retakePhoto() },
                            onApplyCrop = { quad ->"""

content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("ScanDocumentScreen.kt fixed")
