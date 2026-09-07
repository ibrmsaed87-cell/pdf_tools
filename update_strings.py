import os

def add_strings(filepath, strings_dict):
    if not os.path.exists(filepath): return
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    to_insert = ""
    for k, v in strings_dict.items():
        if f'name="{k}"' not in content:
            to_insert += f'    <string name="{k}">{v}</string>\n'
            
    if to_insert:
        content = content.replace("</resources>", to_insert + "</resources>")
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

strings_en = {
    "msg_loading_pdf": "Loading document...",
    "msg_analyzing_pdf": "Analyzing pages...",
    "msg_compressing_image": "Compressing image %1$d of %2$d...",
    "msg_saving_pdf": "Saving PDF..."
}

strings_ar = {
    "msg_loading_pdf": "جاري تحميل المستند...",
    "msg_analyzing_pdf": "جاري تحليل الصفحات...",
    "msg_compressing_image": "جاري ضغط الصورة %1$d من %2$d...",
    "msg_saving_pdf": "جاري حفظ PDF..."
}

strings_es = {
    "msg_loading_pdf": "Cargando documento...",
    "msg_analyzing_pdf": "Analizando páginas...",
    "msg_compressing_image": "Comprimiendo imagen %1$d de %2$d...",
    "msg_saving_pdf": "Guardando PDF..."
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)
