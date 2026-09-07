import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target1 = 'fun HomeScreen(onNavigateToTools: () -> Unit = {}, onNavigateToImageToPdf: () -> Unit = {}) {'
replacement1 = 'fun HomeScreen(onNavigateToTools: () -> Unit = {}, onNavigateToImageToPdf: () -> Unit = {}, onNavigateToScanDocument: () -> Unit = {}) {'
content = content.replace(target1, replacement1)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
