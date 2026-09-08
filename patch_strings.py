import re
import os

strings_to_add_en = """    <string name="text_editor_title">Title</string>
    <string name="text_editor_title_optional">Title (Optional)</string>
    <string name="text_editor_body">Body</string>"""

strings_to_add_ar = """    <string name="text_editor_title">العنوان</string>
    <string name="text_editor_title_optional">العنوان (اختياري)</string>
    <string name="text_editor_body">النص</string>"""

strings_to_add_es = """    <string name="text_editor_title">Título</string>
    <string name="text_editor_title_optional">Título (Opcional)</string>
    <string name="text_editor_body">Texto</string>"""

def patch_file(filepath, strings_to_add):
    if not os.path.exists(filepath):
        return
    with open(filepath, 'r') as f:
        content = f.read()
    if 'text_editor_title' not in content:
        content = content.replace('</resources>', f'{strings_to_add}\n</resources>')
        with open(filepath, 'w') as f:
            f.write(content)

patch_file('app/src/main/res/values/strings.xml', strings_to_add_en)
patch_file('app/src/main/res/values-ar/strings.xml', strings_to_add_ar)
patch_file('app/src/main/res/values-es/strings.xml', strings_to_add_es)
