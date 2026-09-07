import sys

filepath = 'app/src/test/java/com/spinel/pdftools/ui/scandocument/ScanDocumentViewModelTest.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('private lateinit classUnderTest', 'private lateinit var classUnderTest')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Test fixed")
