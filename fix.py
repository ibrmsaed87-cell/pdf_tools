import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    '_state.value = CompressState.Error("Compression failed.',
    '_state.value = CompressState.Error("Compression failed.\\n\\nDiagnostics:\\n${diagnosticsReport}") // '
)

content = content.replace('Diagnostics:\n${diagnosticsReport}")', '')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Fixed")
