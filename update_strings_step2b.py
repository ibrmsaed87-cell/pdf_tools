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
    "action_edit": "Edit",
    "label_font_size": "Font Size",
    "label_text_color": "Color",
    "label_alignment": "Alignment",
    "content_desc_align_start": "Align Start",
    "content_desc_align_center": "Align Center",
    "content_desc_align_end": "Align End",
    "content_desc_bold": "Bold",
    "action_save": "Save"
}

ar = {
    "action_edit": "تعديل",
    "label_font_size": "حجم الخط",
    "label_text_color": "اللون",
    "label_alignment": "المحاذاة",
    "content_desc_align_start": "محاذاة للبداية",
    "content_desc_align_center": "محاذاة للوسط",
    "content_desc_align_end": "محاذاة للنهاية",
    "content_desc_bold": "عريض",
    "action_save": "حفظ"
}

es = {
    "action_edit": "Editar",
    "label_font_size": "Tamaño de fuente",
    "label_text_color": "Color",
    "label_alignment": "Alineación",
    "content_desc_align_start": "Alinear al inicio",
    "content_desc_align_center": "Alinear al centro",
    "content_desc_align_end": "Alinear al final",
    "content_desc_bold": "Negrita",
    "action_save": "Guardar"
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
