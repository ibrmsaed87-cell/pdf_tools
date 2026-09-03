import re

def update_file(path, replacements):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for key, val in replacements.items():
        pattern = r'(<string name="' + key + r'">).*?(</string>)'
        content = re.sub(pattern, r'\g<1>' + val + r'\g<2>', content)
        
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

en = {
    'desc_image_to_pdf': 'Images → PDF',
    'desc_compress_pdf': 'Reduce file size',
    'desc_merge_pdf': 'Combine PDFs',
    'desc_split_pdf': 'Extract pages',
    'desc_pdf_to_jpg': 'PDF → Images',
    'desc_organize_pdf': 'Reorder pages'
}

ar = {
    'desc_image_to_pdf': 'صور → PDF',
    'desc_compress_pdf': 'تقليل حجم الملف',
    'desc_merge_pdf': 'دمج ملفات PDF',
    'desc_split_pdf': 'استخراج الصفحات',
    'desc_pdf_to_jpg': 'PDF → صور',
    'desc_organize_pdf': 'إعادة ترتيب الصفحات'
}

es = {
    'desc_image_to_pdf': 'Imágenes → PDF',
    'desc_compress_pdf': 'Reducir tamaño',
    'desc_merge_pdf': 'Combinar PDFs',
    'desc_split_pdf': 'Extraer páginas',
    'desc_pdf_to_jpg': 'PDF → Imágenes',
    'desc_organize_pdf': 'Reordenar páginas'
}

update_file('app/src/main/res/values/strings.xml', en)
update_file('app/src/main/res/values-ar/strings.xml', ar)
update_file('app/src/main/res/values-es/strings.xml', es)

