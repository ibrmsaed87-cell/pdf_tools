import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

if 'object SplitPdf' not in content:
    target = '    object MergePdf : Screen("merge_pdf", R.string.title_merge_pdf, Icons.AutoMirrored.Filled.MergeType)'
    replacement = target + '\n    object SplitPdf : Screen("split_pdf", R.string.title_split_pdf, Icons.Filled.ContentCut)'
    content = content.replace(target, replacement)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print("Screen patched")
