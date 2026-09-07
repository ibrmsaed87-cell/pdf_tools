import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix the broken error state string
fixed_logic = """                if (result.diagnostics.finalDecision == "NOT_REDUCED") {
                    _state.value = CompressState.NotReduced("The original document is already highly optimized. Compression did not reduce the file size further.", diagnosticsReport)
                } else {
                    _state.value = CompressState.Error("Compression failed.\\n\\nDiagnostics:\\n${diagnosticsReport}")
                }"""

# Actually, the file is broken right now. Let's just rewrite the exact block that is broken.
# Let's see what it looks like now:
