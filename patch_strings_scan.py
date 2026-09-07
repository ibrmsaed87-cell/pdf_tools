import xml.etree.ElementTree as ET

def add_strings(filepath, strings_dict):
    tree = ET.parse(filepath)
    root = tree.getroot()
    
    existing = set([child.attrib.get('name') for child in root])
    
    for name, val in strings_dict.items():
        if name not in existing:
            elem = ET.Element('string', {'name': name})
            elem.text = val
            root.append(elem)
            
    ET.indent(tree, space="    ", level=0)
    tree.write(filepath, encoding='utf-8', xml_declaration=True)

en = {
    'title_scanned_document': 'Scanned Document',
    'action_save_pdf': 'Save PDF',
    'msg_generating_pdf': 'Generating PDF...',
    'msg_pdf_saved': 'PDF saved successfully!',
    'err_pdf_generation_failed': 'Failed to generate PDF',
    'err_no_pages_to_save': 'No pages to save',
    'msg_no_pages': 'No pages scanned yet.',
    'msg_delete_page': 'Delete page?'
}

ar = {
    'title_scanned_document': 'المستند الممسوح',
    'action_save_pdf': 'حفظ PDF',
    'msg_generating_pdf': 'جاري إنشاء PDF...',
    'msg_pdf_saved': 'تم حفظ PDF بنجاح!',
    'err_pdf_generation_failed': 'فشل إنشاء PDF',
    'err_no_pages_to_save': 'لا توجد صفحات للحفظ',
    'msg_no_pages': 'لم يتم مسح أي صفحات بعد.',
    'msg_delete_page': 'حذف الصفحة؟'
}

es = {
    'title_scanned_document': 'Documento Escaneado',
    'action_save_pdf': 'Guardar PDF',
    'msg_generating_pdf': 'Generando PDF...',
    'msg_pdf_saved': '¡PDF guardado exitosamente!',
    'err_pdf_generation_failed': 'Error al generar PDF',
    'err_no_pages_to_save': 'No hay páginas para guardar',
    'msg_no_pages': 'No hay páginas escaneadas.',
    'msg_delete_page': '¿Eliminar página?'
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
print("Strings added")
