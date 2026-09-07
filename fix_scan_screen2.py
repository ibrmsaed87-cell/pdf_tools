import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('stringResource(R.string.action_back)', '"Back"')
content = content.replace('if (quad.isValid())', 'if (CropGeometry.isValidQuadrilateral(quad))')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("ScanDocumentScreen.kt fixed 2")
