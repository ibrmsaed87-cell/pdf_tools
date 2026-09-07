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
    'action_crop_document': 'Crop Document',
    'action_apply_crop': 'Apply Crop',
    'action_edit_crop': 'Edit Crop',
    'action_use_scan': 'Use Scan',
    'msg_invalid_crop': 'Invalid crop area. Please adjust corners.'
}

strings_ar = {
    'action_crop_document': 'قص المستند',
    'action_apply_crop': 'تطبيق القص',
    'action_edit_crop': 'تعديل القص',
    'action_use_scan': 'استخدام المسح',
    'msg_invalid_crop': 'منطقة القص غير صالحة. يرجى ضبط الزوايا.'
}

strings_es = {
    'action_crop_document': 'Recortar Documento',
    'action_apply_crop': 'Aplicar recorte',
    'action_edit_crop': 'Editar recorte',
    'action_use_scan': 'Usar escaneo',
    'msg_invalid_crop': 'Área de recorte inválida. Por favor, ajuste las esquinas.'
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)

print("Strings added")
