import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_title_field = """            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.hint_title_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(titleStyle.color.colorValue),
                    unfocusedTextColor = Color(titleStyle.color.colorValue)
                )
            )"""

replacement_title_field = """            val isDark = androidx.compose.foundation.isSystemInDarkTheme()
            val needsLightBgTitle = isDark && (titleStyle.color == TextColor.Black || titleStyle.color == TextColor.DarkGray)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.hint_title_optional)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(titleStyle.color.colorValue),
                    unfocusedTextColor = Color(titleStyle.color.colorValue),
                    focusedContainerColor = if (needsLightBgTitle) Color(0xFFF5F5F5) else Color.Transparent,
                    unfocusedContainerColor = if (needsLightBgTitle) Color(0xFFF5F5F5) else Color.Transparent
                )
            )"""

target_body_field = """            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.hint_body_required)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(min = 100.dp, max = 200.dp),
                minLines = 4,
                maxLines = 10,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(bodyStyle.color.colorValue),
                    unfocusedTextColor = Color(bodyStyle.color.colorValue)
                )
            )"""

replacement_body_field = """            val needsLightBgBody = isDark && (bodyStyle.color == TextColor.Black || bodyStyle.color == TextColor.DarkGray)
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text(stringResource(R.string.hint_body_required)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(min = 100.dp, max = 200.dp),
                minLines = 4,
                maxLines = 10,
                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(bodyStyle.color.colorValue),
                    unfocusedTextColor = Color(bodyStyle.color.colorValue),
                    focusedContainerColor = if (needsLightBgBody) Color(0xFFF5F5F5) else Color.Transparent,
                    unfocusedContainerColor = if (needsLightBgBody) Color(0xFFF5F5F5) else Color.Transparent
                )
            )"""

target_buttons = """                        Row(
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
                        }"""

replacement_buttons = """                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.action_done))
                            }
                            Button(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(state.outputUri, "application/pdf")
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
                        }"""

content = content.replace(target_title_field, replacement_title_field)
content = content.replace(target_body_field, replacement_body_field)
content = content.replace(target_buttons, replacement_buttons)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
