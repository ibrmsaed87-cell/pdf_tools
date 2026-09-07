import sys
import os

files_to_patch = [
    'app/src/main/java/com/spinel/pdftools/MainActivity.kt',
    'app/src/main/java/com/spinel/pdftools/ui/mergepdf/MergePdfViewModel.kt'
]

for filepath in files_to_patch:
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = content.replace('com.tomroush.pdfbox', 'com.tom_roush.pdfbox')
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
            
print("Patched tomroush to tom_roush")
