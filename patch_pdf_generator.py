import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

def fix_pdf_generator(content):
    # we need to map physical Left/Right to ALIGN_NORMAL/ALIGN_OPPOSITE based on paragraph direction.
    # We will write a helper function inside PdfGenerator.
    
    helper = """
    private fun getPhysicalAlignment(align: TextAlignment, text: String): Layout.Alignment {
        val isRtl = android.text.BidiFormatter.getInstance().isRtl(text)
        return when (align) {
            TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
            TextAlignment.Left -> if (isRtl) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL
            TextAlignment.Right -> if (isRtl) Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_OPPOSITE
        }
    }
"""
    if "private fun getPhysicalAlignment" not in content:
        content = content.replace("object PdfGenerator {", "object PdfGenerator {" + helper)
    
    old_title_align = """                        val titleAlign = when (pageItem.titleStyle.alignment) {
                            TextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
                        }"""
    
    new_title_align = """                        val titleAlign = getPhysicalAlignment(pageItem.titleStyle.alignment, pageItem.title)"""
    content = content.replace(old_title_align, new_title_align)

    old_body_align = """                        val bodyAlign = when (pageItem.bodyStyle.alignment) {
                            TextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
                            TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
                            TextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
                        }"""
    new_body_align = """                        val bodyAlign = getPhysicalAlignment(pageItem.bodyStyle.alignment, pageItem.body)"""
    content = content.replace(old_body_align, new_body_align)

    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/PdfGenerator.kt', fix_pdf_generator)
