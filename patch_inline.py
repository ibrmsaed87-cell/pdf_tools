import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_to_remove = """            val updateStyle: ((TextStyleConfig) -> TextStyleConfig) -> Unit = { modify ->
                if (selectedTab == 0) titleStyle = modify(titleStyle) else bodyStyle = modify(bodyStyle)
            }"""

if target_to_remove in content:
    content = content.replace(target_to_remove, "")
else:
    print("Could not find updateStyle declaration.")

replacements = [
    (
        "onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.Start) } }",
        "onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.Start) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.Start) }"
    ),
    (
        "onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.Center) } }",
        "onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.Center) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.Center) }"
    ),
    (
        "onCheckedChange = { updateStyle { it.copy(alignment = TextAlignment.End) } }",
        "onCheckedChange = { if (selectedTab == 0) titleStyle = titleStyle.copy(alignment = TextAlignment.End) else bodyStyle = bodyStyle.copy(alignment = TextAlignment.End) }"
    ),
    (
        "onCheckedChange = { isBold -> updateStyle { it.copy(isBold = isBold) } }",
        "onCheckedChange = { isBold -> if (selectedTab == 0) titleStyle = titleStyle.copy(isBold = isBold) else bodyStyle = bodyStyle.copy(isBold = isBold) }"
    ),
    (
        "onClick = { updateStyle { if (it.fontSize > 12) it.copy(fontSize = it.fontSize - 1) else it } }",
        "onClick = { if (selectedTab == 0) { if (titleStyle.fontSize > 12) titleStyle = titleStyle.copy(fontSize = titleStyle.fontSize - 1) } else { if (bodyStyle.fontSize > 12) bodyStyle = bodyStyle.copy(fontSize = bodyStyle.fontSize - 1) } }"
    ),
    (
        "onClick = { updateStyle { if (it.fontSize < 40) it.copy(fontSize = it.fontSize + 1) else it } }",
        "onClick = { if (selectedTab == 0) { if (titleStyle.fontSize < 40) titleStyle = titleStyle.copy(fontSize = titleStyle.fontSize + 1) } else { if (bodyStyle.fontSize < 40) bodyStyle = bodyStyle.copy(fontSize = bodyStyle.fontSize + 1) } }"
    ),
    (
        ".clickable { updateStyle { it.copy(color = tColor) } }",
        ".clickable { if (selectedTab == 0) titleStyle = titleStyle.copy(color = tColor) else bodyStyle = bodyStyle.copy(color = tColor) }"
    )
]

for t, r in replacements:
    if t in content:
        content = content.replace(t, r)
    else:
        print("Failed to replace:", t)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
