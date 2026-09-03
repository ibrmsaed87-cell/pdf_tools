with open('app/src/main/java/com/spinel/pdftools/ui/tools/ToolsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace(
    'PremiumPrimaryCard(\n                title = stringResource(id = R.string.action_scan_document),',
    'PremiumPrimaryCard(\n                modifier = Modifier.padding(horizontal = 16.dp),\n                title = stringResource(id = R.string.action_scan_document),'
)

with open('app/src/main/java/com/spinel/pdftools/ui/tools/ToolsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
