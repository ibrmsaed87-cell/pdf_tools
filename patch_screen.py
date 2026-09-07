import sys
import re

filepath = 'app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Update NotReduced state in CompressPdfScreen
not_reduced_old = """                is CompressState.NotReduced -> {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.msg_compression_not_reduced),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.msg_compression_already_optimized),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
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
                }"""

not_reduced_new = """                is CompressState.NotReduced -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.msg_compression_not_needed),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = currentState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(48.dp))

                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(stringResource(R.string.action_done))
                    }
                }"""

# Since msg_compression_not_reduced and msg_compression_already_optimized are being replaced, I'll just regex replace the whole block.
content = re.sub(
    r'is CompressState\.NotReduced -> \{[\s\S]*?OutlinedButton\([\s\S]*?Text\(stringResource\(R\.string\.action_done\)\)\s*\}\s*\}',
    not_reduced_new,
    content
)

# And make sure Icons.Default.Info is imported if not already. It usually is, or I can just use Icons.Default.Info.
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("done")
