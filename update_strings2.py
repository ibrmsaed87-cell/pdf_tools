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
    "action_add_text": "Add text",
    "title_add_text_page": "Add Text Page",
    "hint_title_optional": "Title (optional)",
    "hint_body_required": "Body text",
    "action_cancel": "Cancel",
    "action_add_page": "Add Page",
    "content_desc_text_preview": "Text page preview"
}

ar = {
    "action_add_text": "إضافة نص",
    "title_add_text_page": "إضافة صفحة نص",
    "hint_title_optional": "العنوان (اختياري)",
    "hint_body_required": "النص",
    "action_cancel": "إلغاء",
    "action_add_page": "إضافة الصفحة",
    "content_desc_text_preview": "معاينة صفحة النص"
}

es = {
    "action_add_text": "Agregar texto",
    "title_add_text_page": "Agregar página de texto",
    "hint_title_optional": "Título (opcional)",
    "hint_body_required": "Texto",
    "action_cancel": "Cancelar",
    "action_add_page": "Agregar página",
    "content_desc_text_preview": "Vista previa de la página de texto"
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
