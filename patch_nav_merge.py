import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

if 'object MergePdf' not in content:
    target = '    object ScanDocument : Screen("scan_document", R.string.title_scan_document, Icons.Filled.DocumentScanner)'
    replacement = target + '\n    object MergePdf : Screen("merge_pdf", R.string.title_merge_pdf, Icons.AutoMirrored.Filled.MergeType)'
    content = content.replace(target, replacement)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print("Screen patched")
