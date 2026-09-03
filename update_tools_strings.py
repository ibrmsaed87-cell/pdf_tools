import re

def add_strings(filepath, new_strings):
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
    'tools_subtitle': 'Everything you need for your PDFs',
    'category_create': 'CREATE',
    'category_optimize': 'OPTIMIZE',
    'category_edit_organize': 'EDIT &amp; ORGANIZE',
    'category_convert': 'CONVERT'
}

ar = {
    'tools_subtitle': 'كل ما تحتاجه لملفات PDF',
    'category_create': 'إنشاء',
    'category_optimize': 'تحسين',
    'category_edit_organize': 'تعديل وتنظيم',
    'category_convert': 'تحويل'
}

es = {
    'tools_subtitle': 'Todo lo que necesitas para tus PDFs',
    'category_create': 'CREAR',
    'category_optimize': 'OPTIMIZAR',
    'category_edit_organize': 'EDITAR Y ORGANIZAR',
    'category_convert': 'CONVERTIR'
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
