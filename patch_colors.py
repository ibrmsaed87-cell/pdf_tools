import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_title = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                )"""

replacement_title = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(titleStyle.color.colorValue),
                    unfocusedTextColor = Color(titleStyle.color.colorValue)
                )"""

target_body = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                )"""

replacement_body = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(bodyStyle.color.colorValue),
                    unfocusedTextColor = Color(bodyStyle.color.colorValue)
                )"""

content = content.replace(target_title, replacement_title)
content = content.replace(target_body, replacement_body)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
