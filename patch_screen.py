import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """text = if (currentState is CompressState.Compressing) stringResource(R.string.msg_compressing) else stringResource(R.string.msg_saving)"""
replacement = """text = if (currentState is CompressState.Compressing) currentState.message else stringResource(R.string.msg_saving)"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched Screen")
else:
    print("Target not found in Screen")
