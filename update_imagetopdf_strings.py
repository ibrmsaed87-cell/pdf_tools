import os

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

en = {
    "title_image_to_pdf": "Image to PDF",
    "desc_image_to_pdf_screen": "Select images from your device to combine them into a single PDF document.",
    "action_select_images": "Select images",
    "action_add_more_images": "Add more",
    "action_create_pdf": "Create PDF",
    "state_creating_pdf": "Creating PDF — %1$d of %2$d",
    "state_success_title": "PDF created successfully!",
    "state_success_desc": "Your new document has been saved.",
    "action_done": "Done",
    "error_generic": "Something went wrong.",
    "error_no_images": "Please select at least one image.",
    "content_desc_image_preview": "Selected image preview",
    "content_desc_remove_image": "Remove image",
    "page_number": "Page %1$d"
}

ar = {
    "title_image_to_pdf": "الصور إلى PDF",
    "desc_image_to_pdf_screen": "حدد صورًا من جهازك لدمجها في مستند PDF واحد.",
    "action_select_images": "تحديد الصور",
    "action_add_more_images": "إضافة المزيد",
    "action_create_pdf": "إنشاء PDF",
    "state_creating_pdf": "جاري إنشاء PDF — %1$d من %2$d",
    "state_success_title": "تم إنشاء PDF بنجاح!",
    "state_success_desc": "تم حفظ المستند الجديد الخاص بك.",
    "action_done": "تم",
    "error_generic": "حدث خطأ ما.",
    "error_no_images": "يرجى تحديد صورة واحدة على الأقل.",
    "content_desc_image_preview": "معاينة الصورة المحددة",
    "content_desc_remove_image": "إزالة الصورة",
    "page_number": "صفحة %1$d"
}

es = {
    "title_image_to_pdf": "Imagen a PDF",
    "desc_image_to_pdf_screen": "Selecciona imágenes de tu dispositivo para combinarlas en un solo documento PDF.",
    "action_select_images": "Seleccionar imágenes",
    "action_add_more_images": "Añadir más",
    "action_create_pdf": "Crear PDF",
    "state_creating_pdf": "Creando PDF — %1$d de %2$d",
    "state_success_title": "¡PDF creado con éxito!",
    "state_success_desc": "Tu nuevo documento ha sido guardado.",
    "action_done": "Hecho",
    "error_generic": "Algo salió mal.",
    "error_no_images": "Por favor selecciona al menos una imagen.",
    "content_desc_image_preview": "Vista previa de la imagen seleccionada",
    "content_desc_remove_image": "Eliminar imagen",
    "page_number": "Página %1$d"
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
