import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Let's just fix the Error block completely.
target = """            } else {
                _state.value = CompressState.Error("Compression failed.$diagnosticsReport")
            }"""

if target in content:
    content = content.replace(target, """            } else {
                _state.value = CompressState.Error("Compression failed: ${diagnosticsReport}")
            }""")
else:
    # Just do a generic fix for the line containing CompressState.Error("Compression failed
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if 'CompressState.Error("Compression failed' in line:
            lines[i] = '                _state.value = CompressState.Error("Compression failed: ${diagnosticsReport}")'
    content = '\n'.join(lines)
    
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed VM")
