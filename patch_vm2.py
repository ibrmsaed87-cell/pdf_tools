import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update CompressState
content = re.sub(
    r'data class NotReduced\(val message: String, val diagnostics: String = ""\) : CompressState\(\)',
    r'data class NotReduced(val message: String) : CompressState()',
    content
)

# 2. Remove diagnosticsReport passing
content = content.replace(
    '_state.value = CompressState.NotReduced("The original document is already highly optimized. Compression did not reduce the file size further.", diagnosticsReport)',
    '_state.value = CompressState.NotReduced("msg_compression_not_needed")' # We can just pass a key or resolve the string in UI, but ViewModel shouldn't have android context for getString directly unless we use getApplication().getString.
)

# Wait, the ViewModel HAS getApplication()!
new_not_reduced = '_state.value = CompressState.NotReduced(getApplication<Application>().getString(com.spinel.pdftools.R.string.msg_compression_not_needed_desc))'
content = content.replace(
    '_state.value = CompressState.NotReduced("msg_compression_not_needed")',
    new_not_reduced
)

# Fix the Error state
content = content.replace(
    '_state.value = CompressState.Error("Compression failed.\\n\\nDiagnostics:\\n${diagnosticsReport}") // ',
    '_state.value = CompressState.Error("Compression failed.")'
)

# Also wait, the first replace didn't have the original string because I did it earlier. Let's do a more robust replace.
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("done")
