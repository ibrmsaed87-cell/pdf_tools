import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

if 'import androidx.compose.material.icons.automirrored.filled.MergeType' not in content:
    target = 'import androidx.compose.material.icons.filled.Image'
    replacement = target + '\nimport androidx.compose.material.icons.automirrored.filled.MergeType'
    content = content.replace(target, replacement)
    
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
print("Screen import patched")
