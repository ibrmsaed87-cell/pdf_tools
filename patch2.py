import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """                    is GenerationState.Success -> {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "PDF saved successfully!",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("OK")
                        }
                    }"""

replacement = """                    is GenerationState.Success -> {
                        val context = LocalContext.current
                        Icon(
                            androidx.compose.material.icons.filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.msg_pdf_saved),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = onDismiss) {
                                Text(stringResource(R.string.action_done))
                            }
                            Button(onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setDataAndType(state.outputUri, "application/pdf")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }) {
                                Text(stringResource(R.string.action_open))
                            }
                        }
                    }"""

if target in content:
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content.replace(target, replacement))
    print("PATCH APPLIED")
else:
    print("TARGET NOT FOUND")
