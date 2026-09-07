import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

new_logic = """            if (compressedFile != null) {
                _state.value = CompressState.Compressing(1f, "Ready to save")
            } else {
                if (result.diagnostics.finalDecision == "NOT_REDUCED") {
                    _state.value = CompressState.NotReduced("The original document is already highly optimized. Compression did not reduce the file size further.", diagnosticsReport)
                } else {
                    _state.value = CompressState.Error("Compression failed.\\n\\nDiagnostics:\\n${diagnosticsReport}")
                }
            }"""

content = re.sub(
    r'if \(compressedFile != null\) \{[\s\S]*?\} else \{\s*_state\.value = CompressState\.Error\("Compression failed: \$\{diagnosticsReport\}"\)\s*\}',
    new_logic,
    content
)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated ViewModel")
