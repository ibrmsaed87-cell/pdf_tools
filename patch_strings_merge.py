import sys
import xml.etree.ElementTree as ET

def add_strings(filepath, strings):
    tree = ET.parse(filepath)
    root = tree.getroot()
    
    existing = set([child.attrib.get('name') for child in root])
    
    for name, val in strings.items():
        if name not in existing:
            elem = ET.Element('string', {'name': name})
            elem.text = val
            root.append(elem)
            
    ET.indent(tree, space="    ", level=0)
    tree.write(filepath, encoding='utf-8', xml_declaration=True)

strings_en = {
    'title_merge_pdf': 'Merge PDF',
    'action_select_pdfs': 'Select PDFs',
    'action_add_pdfs': 'Add PDFs',
    'msg_min_pdfs_required': 'At least two PDFs are required',
    'action_merge_pdfs': 'Merge PDFs',
    'action_remove': 'Remove',
    'msg_pdf_saved_successfully': 'PDF saved successfully!',
    'action_open': 'Open',
    'action_done': 'Done',
    'err_unable_to_read_pdf': 'Unable to read PDF: %s',
    'err_unable_to_merge_pdf': 'Unable to merge PDFs',
    'desc_merge_pdf': 'Combine multiple PDFs'
}

strings_ar = {
    'title_merge_pdf': 'دمج PDF',
    'action_select_pdfs': 'تحديد ملفات PDF',
    'action_add_pdfs': 'إضافة ملفات PDF',
    'msg_min_pdfs_required': 'مطلوب ملفي PDF على الأقل',
    'action_merge_pdfs': 'دمج ملفات PDF',
    'action_remove': 'إزالة',
    'msg_pdf_saved_successfully': 'تم حفظ ملف PDF بنجاح!',
    'action_open': 'فتح',
    'action_done': 'تم',
    'err_unable_to_read_pdf': 'تعذر قراءة ملف PDF: %s',
    'err_unable_to_merge_pdf': 'تعذر دمج ملفات PDF',
    'desc_merge_pdf': 'دمج ملفات PDF متعددة'
}

strings_es = {
    'title_merge_pdf': 'Unir PDF',
    'action_select_pdfs': 'Seleccionar PDFs',
    'action_add_pdfs': 'Agregar PDFs',
    'msg_min_pdfs_required': 'Se requieren al menos dos PDFs',
    'action_merge_pdfs': 'Unir PDFs',
    'action_remove': 'Eliminar',
    'msg_pdf_saved_successfully': '¡PDF guardado con éxito!',
    'action_open': 'Abrir',
    'action_done': 'Hecho',
    'err_unable_to_read_pdf': 'No se pudo leer el PDF: %s',
    'err_unable_to_merge_pdf': 'No se pudo unir los PDFs',
    'desc_merge_pdf': 'Combina múltiples PDFs'
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)

print("Strings added")
