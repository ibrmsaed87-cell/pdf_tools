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

en = { "hint_reorder_pages": "Press and hold to reorder" }
ar = { "hint_reorder_pages": "اضغط مطولاً واسحب لإعادة الترتيب" }
es = { "hint_reorder_pages": "Mantén pulsado y arrastra para reordenar" }

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
