import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """            val updateStyle = { modify: (TextStyleConfig) -> TextStyleConfig ->
                if (selectedTab == 0) titleStyle = modify(titleStyle) else bodyStyle = modify(bodyStyle)
            }"""

replacement = """            val updateStyle: ((TextStyleConfig) -> TextStyleConfig) -> Unit = { modify ->
                if (selectedTab == 0) titleStyle = modify(titleStyle) else bodyStyle = modify(bodyStyle)
            }"""

if target in content:
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content.replace(target, replacement))
    print("PATCH APPLIED")
else:
    print("TARGET NOT FOUND")
