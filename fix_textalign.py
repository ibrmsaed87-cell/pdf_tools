import re

def update_file(path):
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace("TextAlign.Start", "TextAlign.Left")
    content = content.replace("TextAlign.End", "TextAlign.Right")
    with open(path, 'w') as f:
        f.write(content)

update_file('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt')
update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt')
