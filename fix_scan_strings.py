import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('text = "Retake",', 'text = stringResource(R.string.action_retake),')
content = content.replace('text = "Use Photo",', 'text = stringResource(R.string.action_use_photo),')
content = content.replace('text = "Edit Crop",', 'text = stringResource(R.string.action_edit_crop),')
content = content.replace('text = "Use Scan",', 'text = stringResource(R.string.action_use_scan),')
content = content.replace('text = "Back"', 'text = stringResource(R.string.action_cancel)') # it was 'Back' in TopAppBar, let's revert it or just use action_back which failed earlier. Wait, action_cancel is in strings.xml

# also add the Success state UI handling
target_saving = """                    is ScanState.SavingPdf -> {
                        BackHandler { /* Disable back during save */ }
                        SavingPdfView(progress = scanState.progress)
                    }
                }"""

replacement_saving = """                    is ScanState.SavingPdf -> {
                        BackHandler { /* Disable back during save */ }
                        SavingPdfView(progress = scanState.progress)
                    }
                    is ScanState.Success -> {
                        BackHandler { onNavigateBack() }
                        ScanSuccessView(
                            uri = scanState.uri,
                            onDone = { onNavigateBack() }
                        )
                    }
                }"""
content = content.replace(target_saving, replacement_saving)

success_view_code = """
@Composable
fun ScanSuccessView(
    uri: Uri,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_pdf_saved),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_done))
            }
            Button(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_open))
            }
        }
    }
}
"""

content += success_view_code

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("ScanDocumentScreen.kt strings and success view fixed")
