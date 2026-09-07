import sys, re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'is CompressState\.NotReduced -> \{.*?(?=is CompressState\.Error -> |\}$)'

replacement = """is CompressState.NotReduced -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.msg_compression_not_reduced),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (currentState.diagnostics.isNotEmpty()) {
                        androidx.compose.foundation.text.selection.SelectionContainer {
                            Text(
                                text = currentState.diagnostics,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(8.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(stringResource(R.string.action_done))
                    }
                }
                """

if re.search(pattern, content, re.DOTALL):
    content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched Screen via regex")
else:
    print("NotReduced block not found via regex")

