import os

filepath = 'app/src/main/res/values/strings.xml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

en = {
    "tab_title": "Title",
    "tab_body": "Body",
    "action_create_pdf": "Create PDF",
    "title_image_to_pdf": "Image to PDF",
    "action_add_images": "Add images",
    "msg_no_images_selected": "No images selected"
}

ar = {
    "tab_title": "العنوان",
    "tab_body": "النص",
    "action_create_pdf": "إنشاء PDF",
    "title_image_to_pdf": "صورة إلى PDF",
    "action_add_images": "إضافة صور",
    "msg_no_images_selected": "لم يتم تحديد صور"
}

es = {
    "tab_title": "Título",
    "tab_body": "Cuerpo",
    "action_create_pdf": "Crear PDF",
    "title_image_to_pdf": "Imagen a PDF",
    "action_add_images": "Agregar imágenes",
    "msg_no_images_selected": "No hay imágenes seleccionadas"
}

def add_strings(filepath, new_strings):
    if not os.path.exists(filepath): return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    insert_str = ""
    for k, v in new_strings.items():
        if f'<string name="{k}">' not in content:
            insert_str += f'    <string name="{k}">{v}</string>\n'
            
    if insert_str:
        content = content.replace('</resources>', f'{insert_str}</resources>')
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
