import re

with open('app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Update signature
content = content.replace('fun HomeScreen(onNavigateToTools: () -> Unit = {}) {', 'fun HomeScreen(onNavigateToTools: () -> Unit = {}, onNavigateToImageToPdf: () -> Unit = {}) {')

# Find Image to PDF action block
target_card = r'(titleResId = R\.string\.action_image_to_pdf,.*?onClick = )\{ /\* Coming soon \*/ \}'
content = re.sub(target_card, r'\1onNavigateToImageToPdf', content, flags=re.DOTALL)

# Let's just be sure it's correct for the others if they exist, but we only replace image_to_pdf
with open('app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)


with open('app/src/main/java/com/spinel/pdftools/ui/tools/ToolsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('fun ToolsScreen() {', 'fun ToolsScreen(onNavigateToImageToPdf: () -> Unit = {}) {')

target_card = r'(title = stringResource\(id = tool\.titleResId\),\s*description = stringResource\(id = tool\.descResId\),\s*icon = tool\.icon,\s*iconContainerColor = tool\.iconContainerColor,\s*onClick = )\{ /\* Coming soon \*/ \}'

# In ToolsScreen, it iterates over category.tools! So we need to check if tool.titleResId == R.string.action_image_to_pdf
tools_card_replacement = r"""onClick = { 
                        if (tool.titleResId == R.string.action_image_to_pdf) {
                            onNavigateToImageToPdf()
                        } else {
                            /* Coming soon */
                        }
                    }"""

content = re.sub(target_card, r'\1' + tools_card_replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/spinel/pdftools/ui/tools/ToolsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
