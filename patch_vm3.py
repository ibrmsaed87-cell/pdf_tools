import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'CompressState.Error("Invalid PDF file or size is 0.")',
    'CompressState.Error(getApplication<Application>().getString(com.spinel.pdftools.R.string.error_invalid_pdf_size))'
)

content = content.replace(
    'CompressState.Compressing(0f, "Preparing...")',
    'CompressState.Compressing(0f, getApplication<Application>().getString(com.spinel.pdftools.R.string.msg_preparing))'
)

content = content.replace(
    'CompressState.Compressing(1f, "Ready to save")',
    'CompressState.Compressing(1f, getApplication<Application>().getString(com.spinel.pdftools.R.string.msg_ready_to_save))'
)

content = content.replace(
    'CompressState.Error("Compression failed.")',
    'CompressState.Error(getApplication<Application>().getString(com.spinel.pdftools.R.string.error_compression_failed))'
)

content = content.replace(
    'CompressState.Error("Failed to save the file.")',
    'CompressState.Error(getApplication<Application>().getString(com.spinel.pdftools.R.string.error_failed_to_save))'
)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("done")
