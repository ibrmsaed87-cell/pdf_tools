import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/CropComponents.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'Canvas('
replacement = 'val primaryColor = MaterialTheme.colorScheme.primary\n                Canvas('

content = content.replace(target, replacement)

content = content.replace('MaterialTheme.colorScheme.primary, radius * 0.4f', 'primaryColor, radius * 0.4f')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
