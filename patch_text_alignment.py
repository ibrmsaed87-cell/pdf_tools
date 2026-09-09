import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

def fix_document_page(content):
    return content.replace(
        "enum class TextAlignment { Start, Center, End }",
        "enum class TextAlignment { Left, Center, Right }"
    ).replace(
        "val alignment: TextAlignment = TextAlignment.Start",
        "val alignment: TextAlignment = TextAlignment.Left"
    )

update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/DocumentPage.kt', fix_document_page)

def fix_pdf_generator(content):
    content = content.replace("TextAlignment.Start", "TextAlignment.Left")
    content = content.replace("TextAlignment.End", "TextAlignment.Right")
    # In Android StaticLayout, ALIGN_NORMAL aligns to start of paragraph.
    # To force physical left/right regardless of paragraph direction:
    # Android StaticLayout doesn't have ALIGN_LEFT/ALIGN_RIGHT until API 28.
    # However, since we want physical left/right, we can use ALIGN_NORMAL for left in LTR.
    # Wait, in API 21+, we can use BidiFormatter or just use the ALIGN_NORMAL/ALIGN_OPPOSITE but we have to know paragraph direction.
    # Actually, Android Layout.Alignment enum has ALIGN_NORMAL, ALIGN_OPPOSITE, ALIGN_CENTER.
    # If we use ALIGN_NORMAL and text is Arabic, it aligns RIGHT. 
    # If text is English, it aligns LEFT.
    # To FORCE physical LEFT/RIGHT on StaticLayout, we need to set the text direction heuristically or explicitly?
    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/PdfGenerator.kt', fix_pdf_generator)
