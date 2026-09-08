import sys
import xml.etree.ElementTree as ET

def add_strings(file_path, new_strings):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        existing_names = set([elem.attrib['name'] for elem in root.findall('string')])
        
        changed = False
        for name, value in new_strings.items():
            if name not in existing_names:
                new_elem = ET.SubElement(root, 'string', {'name': name})
                new_elem.text = value
                changed = True
                
        if changed:
            ET.indent(tree, space="    ", level=0)
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print(f"Updated {file_path}")
    except Exception as e:
        print(f"Error processing {file_path}: {e}")

strings_en = {
    "action_create_pdf": "Create PDF",
    "desc_create_pdf": "Create a new document from scratch",
    "title_create_pdf": "Create PDF",
    "action_add_text": "Add Text",
    "action_add_images": "Add Images",
    "action_edit_text": "Edit text",
    "msg_empty_document": "New document",
    "msg_creating_pdf": "Creating PDF...",
    "action_view_pdf": "View PDF",
    "err_blank_text_page": "Text page cannot be empty"
}

strings_ar = {
    "action_create_pdf": "إنشاء PDF",
    "desc_create_pdf": "إنشاء مستند جديد من الصفر",
    "title_create_pdf": "إنشاء PDF",
    "action_add_text": "إضافة نص",
    "action_add_images": "إضافة صور",
    "action_edit_text": "تعديل النص",
    "msg_empty_document": "مستند جديد",
    "msg_creating_pdf": "يتم إنشاء PDF...",
    "action_view_pdf": "عرض PDF",
    "err_blank_text_page": "لا يمكن أن تكون صفحة النص فارغة"
}

strings_es = {
    "action_create_pdf": "Crear PDF",
    "desc_create_pdf": "Crear un nuevo documento desde cero",
    "title_create_pdf": "Crear PDF",
    "action_add_text": "Añadir texto",
    "action_add_images": "Añadir imágenes",
    "action_edit_text": "Editar texto",
    "msg_empty_document": "Nuevo documento",
    "msg_creating_pdf": "Creando PDF...",
    "action_view_pdf": "Ver PDF",
    "err_blank_text_page": "La página de texto no puede estar vacía"
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)
