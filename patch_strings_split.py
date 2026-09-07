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
    'title_split_pdf': 'Split PDF',
    'action_select_pdf': 'Select PDF',
    'action_change_pdf': 'Change PDF',
    'desc_split_pdf': 'Extract pages from a PDF',
    'msg_total_pages': '%d pages',
    'msg_selection_mode': 'Selection Mode',
    'mode_all_pages': 'All pages',
    'mode_custom_pages': 'Custom pages',
    'label_page_range': 'Page range',
    'msg_page_range_hint': 'E.g., 1-3, 5, 8-10',
    'action_extract_pages': 'Extract selected pages',
    'err_invalid_page_range': 'Invalid page range',
    'err_page_does_not_exist': 'Page does not exist',
    'err_at_least_one_page': 'At least one page is required',
    'msg_extracting_pages': 'Extracting pages...',
    'err_extraction_failed': 'Extraction failed'
}

strings_ar = {
    'title_split_pdf': 'تقسيم PDF',
    'action_select_pdf': 'تحديد ملف PDF',
    'action_change_pdf': 'تغيير ملف PDF',
    'desc_split_pdf': 'استخراج صفحات من ملف PDF',
    'msg_total_pages': '%d صفحات',
    'msg_selection_mode': 'وضع التحديد',
    'mode_all_pages': 'كل الصفحات',
    'mode_custom_pages': 'صفحات مخصصة',
    'label_page_range': 'نطاق الصفحات',
    'msg_page_range_hint': 'مثال: 1-3، 5، 8-10',
    'action_extract_pages': 'استخراج الصفحات المحددة',
    'err_invalid_page_range': 'نطاق صفحات غير صالح',
    'err_page_does_not_exist': 'الصفحة غير موجودة',
    'err_at_least_one_page': 'مطلوب صفحة واحدة على الأقل',
    'msg_extracting_pages': 'جاري استخراج الصفحات...',
    'err_extraction_failed': 'فشل الاستخراج'
}

strings_es = {
    'title_split_pdf': 'Dividir PDF',
    'action_select_pdf': 'Seleccionar PDF',
    'action_change_pdf': 'Cambiar PDF',
    'desc_split_pdf': 'Extraer páginas de un PDF',
    'msg_total_pages': '%d páginas',
    'msg_selection_mode': 'Modo de selección',
    'mode_all_pages': 'Todas las páginas',
    'mode_custom_pages': 'Páginas personalizadas',
    'label_page_range': 'Rango de páginas',
    'msg_page_range_hint': 'Ej., 1-3, 5, 8-10',
    'action_extract_pages': 'Extraer páginas seleccionadas',
    'err_invalid_page_range': 'Rango de páginas inválido',
    'err_page_does_not_exist': 'La página no existe',
    'err_at_least_one_page': 'Se requiere al menos una página',
    'msg_extracting_pages': 'Extrayendo páginas...',
    'err_extraction_failed': 'La extracción falló'
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)

print("Strings added")
