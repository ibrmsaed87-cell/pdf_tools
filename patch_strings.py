import sys
import xml.etree.ElementTree as ET

def add_strings(filepath, strings):
    tree = ET.parse(filepath)
    root = tree.getroot()
    
    # Check if they exist
    existing = set([child.attrib.get('name') for child in root])
    
    for name, val in strings.items():
        if name not in existing:
            elem = ET.Element('string', {'name': name})
            elem.text = val
            root.append(elem)
            
    ET.indent(tree, space="    ", level=0)
    tree.write(filepath, encoding='utf-8', xml_declaration=True)

strings_en = {
    'permission_camera_rationale': 'Camera access is required to scan documents.',
    'action_grant_permission': 'Grant Permission',
    'action_retake': 'Retake',
    'action_use_photo': 'Use Photo',
    'msg_scan_accepted': 'Document scanned and saved successfully!',
    'action_take_photo': 'Take Photo',
    'title_scan_document': 'Scan Document'
}

strings_ar = {
    'permission_camera_rationale': 'الوصول إلى الكاميرا مطلوب لمسح المستندات ضوئياً.',
    'action_grant_permission': 'منح الإذن',
    'action_retake': 'إعادة التقاط',
    'action_use_photo': 'استخدام الصورة',
    'msg_scan_accepted': 'تم مسح المستند ضوئياً وحفظه بنجاح!',
    'action_take_photo': 'التقاط صورة',
    'title_scan_document': 'مسح مستند'
}

strings_es = {
    'permission_camera_rationale': 'Se requiere acceso a la cámara para escanear documentos.',
    'action_grant_permission': 'Conceder permiso',
    'action_retake': 'Volver a tomar',
    'action_use_photo': 'Usar foto',
    'msg_scan_accepted': '¡Documento escaneado y guardado con éxito!',
    'action_take_photo': 'Tomar foto',
    'title_scan_document': 'Escanear Documento'
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)

print("Strings added")
