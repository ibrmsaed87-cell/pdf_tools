with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('Text("Title", modifier = Modifier.padding(16.dp))', 'Text(stringResource(R.string.text_editor_title), modifier = Modifier.padding(16.dp))')
content = content.replace('Text("Body", modifier = Modifier.padding(16.dp))', 'Text(stringResource(R.string.text_editor_body), modifier = Modifier.padding(16.dp))')
content = content.replace('label = { Text("Title (Optional)") }', 'label = { Text(stringResource(R.string.text_editor_title_optional)) }')
content = content.replace('label = { Text("Body") }', 'label = { Text(stringResource(R.string.text_editor_body)) }')

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'w') as f:
    f.write(content)
