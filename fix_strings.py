import os

filepath = 'app/src/main/res/values/strings.xml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Make sure these exist in all strings files
en = {
    "action_add_images": "Add images",
    "msg_no_images_selected": "No images selected",
    "action_remove_image": "Remove image",
    "msg_generating_pdf": "Generating PDF...",
    "msg_pdf_saved": "PDF saved successfully!",
    "msg_error_generating": "Error generating PDF"
}

ar = {
    "action_add_images": "إضافة صور",
    "msg_no_images_selected": "لم يتم تحديد صور",
    "action_remove_image": "إزالة الصورة",
    "msg_generating_pdf": "جاري إنشاء ملف PDF...",
    "msg_pdf_saved": "تم حفظ ملف PDF بنجاح!",
    "msg_error_generating": "خطأ في إنشاء ملف PDF"
}

es = {
    "action_add_images": "Agregar imágenes",
    "msg_no_images_selected": "No hay imágenes seleccionadas",
    "action_remove_image": "Eliminar imagen",
    "msg_generating_pdf": "Generando PDF...",
    "msg_pdf_saved": "PDF guardado correctamente!",
    "msg_error_generating": "Error al generar el PDF"
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
