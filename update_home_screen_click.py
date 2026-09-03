import re

with open('app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target_card = r'(title = stringResource\(id = tool\.titleResId\),\s*description = stringResource\(id = tool\.descResId\),\s*icon = tool\.icon,\s*iconContainerColor = tool\.iconContainerColor,\s*onClick = )\{ /\* Coming soon \*/ \}'

tools_card_replacement = r"""onClick = { 
                    if (tool.titleResId == R.string.action_image_to_pdf) {
                        onNavigateToImageToPdf()
                    } else {
                        /* Coming soon */
                    }
                }"""

content = re.sub(target_card, r'\1' + tools_card_replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
