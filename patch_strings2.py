import sys
import xml.etree.ElementTree as ET
import os

def update_strings(filepath, new_strings, modifications):
    tree = ET.parse(filepath)
    root = tree.getroot()
    
    # Update existing
    for string_elem in root.findall('string'):
        name = string_elem.get('name')
        if name in modifications:
            string_elem.text = modifications[name]
            
    # Add new
    existing_names = set(elem.get('name') for elem in root.findall('string'))
    for key, value in new_strings.items():
        if key not in existing_names:
            new_elem = ET.Element('string', name=key)
            new_elem.text = value
            root.append(new_elem)
            
    tree.write(filepath, encoding='utf-8', xml_declaration=True)

en_new = {
    'msg_preparing': 'Preparing...',
    'msg_ready_to_save': 'Ready to save',
    'error_compression_failed': 'Compression failed.',
    'error_invalid_pdf_size': 'Invalid PDF file or size is 0.',
    'error_failed_to_save': 'Failed to save the file.'
}
en_mod = {}

ar_new = {
    'msg_preparing': 'جارٍ التحضير...',
    'msg_ready_to_save': 'جاهز للحفظ',
    'error_compression_failed': 'فشل الضغط.',
    'error_invalid_pdf_size': 'ملف PDF غير صالح أو حجمه 0.',
    'error_failed_to_save': 'فشل في حفظ الملف.'
}
ar_mod = {}

es_new = {
    'msg_preparing': 'Preparando...',
    'msg_ready_to_save': 'Listo para guardar',
    'error_compression_failed': 'La compresión falló.',
    'error_invalid_pdf_size': 'Archivo PDF no válido o el tamaño es 0.',
    'error_failed_to_save': 'Error al guardar el archivo.'
}
es_mod = {}

update_strings('app/src/main/res/values/strings.xml', en_new, en_mod)
update_strings('app/src/main/res/values-ar/strings.xml', ar_new, ar_mod)
update_strings('app/src/main/res/values-es/strings.xml', es_new, es_mod)
print("Updated strings.xml")
