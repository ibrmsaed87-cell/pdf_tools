import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

import re
match = re.search(r'is CompressState\.NotReduced.*?\}', content, re.DOTALL)
if match:
    print("Found NotReduced block:")
    print(match.group(0))
else:
    print("NotReduced block not found in exactly this format, looking for NotReduced...")
    match = re.search(r'is CompressState\.NotReduced.*?\}', content, re.DOTALL)
    print("Checking lines...")
    for i, line in enumerate(content.split('\n')):
        if 'NotReduced' in line:
            print(f"{i}: {line}")

