import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """            val activeStyle = if (selectedTab == 0) titleStyle else bodyStyle
            val updateActiveStyle = { newStyle: TextStyleConfig ->
                if (selectedTab == 0) titleStyle = newStyle else bodyStyle = newStyle
            }

            // Formatting Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    val scaleX = if (isRtl) -1f else 1f

                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Start,
                        onCheckedChange = { updateActiveStyle(activeStyle.copy(alignment = TextAlignment.Start)) }
                    ) {
                        Icon(Icons.Filled.FormatAlignLeft, contentDescription = stringResource(R.string.content_desc_align_start), modifier = Modifier.scale(scaleX))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Center,
                        onCheckedChange = { updateActiveStyle(activeStyle.copy(alignment = TextAlignment.Center)) }
                    ) {
                        Icon(Icons.Filled.FormatAlignCenter, contentDescription = stringResource(R.string.content_desc_align_center))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.End,
                        onCheckedChange = { updateActiveStyle(activeStyle.copy(alignment = TextAlignment.End)) }
                    ) {
                        Icon(Icons.Filled.FormatAlignRight, contentDescription = stringResource(R.string.content_desc_align_end), modifier = Modifier.scale(scaleX))
                    }
                }

                // Bold
                IconToggleButton(
                    checked = activeStyle.isBold,
                    onCheckedChange = { updateActiveStyle(activeStyle.copy(isBold = it)) }
                ) {
                    Icon(Icons.Filled.FormatBold, contentDescription = stringResource(R.string.content_desc_bold))
                }
                
                // Font Size
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (activeStyle.fontSize > 12) updateActiveStyle(activeStyle.copy(fontSize = activeStyle.fontSize - 1)) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }
                    Text("${activeStyle.fontSize}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { if (activeStyle.fontSize < 40) updateActiveStyle(activeStyle.copy(fontSize = activeStyle.fontSize + 1)) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Color Palette
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TextColor.values()) { tColor ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(tColor.colorValue))
                            .border(
                                width = if (activeStyle.color == tColor) 3.dp else 1.dp,
                                color = if (activeStyle.color == tColor) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { updateActiveStyle(activeStyle.copy(color = tColor)) }
                    )
                }
            }"""

replacement = """            val activeStyle = if (selectedTab == 0) titleStyle else bodyStyle
            val updateStyle = { modify: (TextStyleConfig) -> TextStyleConfig ->
                if (selectedTab == 0) titleStyle = modify(titleStyle) else bodyStyle = modify(bodyStyle)
            }

            // Formatting Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alignment
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                    val scaleX = if (isRtl) -1f else 1f

                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Start,
                        onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.Start) } }
                    ) {
                        Icon(Icons.Filled.FormatAlignLeft, contentDescription = stringResource(R.string.content_desc_align_start), modifier = Modifier.scale(scaleX))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.Center,
                        onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.Center) } }
                    ) {
                        Icon(Icons.Filled.FormatAlignCenter, contentDescription = stringResource(R.string.content_desc_align_center))
                    }
                    IconToggleButton(
                        checked = activeStyle.alignment == TextAlignment.End,
                        onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.End) } }
                    ) {
                        Icon(Icons.Filled.FormatAlignRight, contentDescription = stringResource(R.string.content_desc_align_end), modifier = Modifier.scale(scaleX))
                    }
                }

                // Bold
                IconToggleButton(
                    checked = activeStyle.isBold,
                    onCheckedChange = { isBold -> updateStyle { it.copy(isBold = isBold) } }
                ) {
                    Icon(Icons.Filled.FormatBold, contentDescription = stringResource(R.string.content_desc_bold))
                }
                
                // Font Size
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { updateStyle { if (it.fontSize > 12) it.copy(fontSize = it.fontSize - 1) else it } }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }
                    Text("${activeStyle.fontSize}", style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { updateStyle { if (it.fontSize < 40) it.copy(fontSize = it.fontSize + 1) else it } }) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Color Palette
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TextColor.values()) { tColor ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(tColor.colorValue))
                            .border(
                                width = if (activeStyle.color == tColor) 3.dp else 1.dp,
                                color = if (activeStyle.color == tColor) MaterialTheme.colorScheme.primary else Color.LightGray,
                                shape = CircleShape
                            )
                            .clickable { updateStyle { it.copy(color = tColor) } }
                    )
                }
            }"""

if target in content:
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content.replace(target, replacement))
    print("PATCH APPLIED")
else:
    print("TARGET NOT FOUND")
