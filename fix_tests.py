import re

def update_file(path):
    with open(path, 'r') as f:
        content = f.read()
    content = content.replace("TextAlignment.Start", "TextAlignment.Left")
    content = content.replace("TextAlignment.End", "TextAlignment.Right")
    with open(path, 'w') as f:
        f.write(content)

update_file('app/src/test/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfViewModelTest.kt')
update_file('app/src/test/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfViewModelTest2.kt')
