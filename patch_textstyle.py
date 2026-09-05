import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_title_style = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),"""

replacement_title_style = """                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = titleAlign,
                    fontSize = titleStyle.fontSize.sp,
                    fontWeight = if (titleStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(titleStyle.color.colorValue)
                ),"""

target_body_style = """                textStyle = LocalTextStyle.current.copy(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),"""

replacement_body_style = """                textStyle = androidx.compose.ui.text.TextStyle(
                    textAlign = bodyAlign,
                    fontSize = bodyStyle.fontSize.sp,
                    fontWeight = if (bodyStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                    color = Color(bodyStyle.color.colorValue)
                ),"""

content = content.replace(target_title_style, replacement_title_style)
content = content.replace(target_body_style, replacement_body_style)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
