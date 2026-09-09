import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

def fix_screen(content):
    content = content.replace("TextAlignment.Start", "TextAlignment.Left")
    content = content.replace("TextAlignment.End", "TextAlignment.Right")
    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', fix_screen)
update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt', fix_screen)
