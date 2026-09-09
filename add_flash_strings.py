import re
def add_to_file(filepath, strings_to_add):
    with open(filepath, 'r') as f:
        content = f.read()
    if "</resources>" in content:
        new_content = content.replace("</resources>", strings_to_add + "\n</resources>")
        with open(filepath, 'w') as f:
            f.write(new_content)

add_to_file('app/src/main/res/values/strings.xml', '    <string name="action_flash_on">Turn Flash On</string>\n    <string name="action_flash_off">Turn Flash Off</string>')
add_to_file('app/src/main/res/values-ar/strings.xml', '    <string name="action_flash_on">تشغيل الفلاش</string>\n    <string name="action_flash_off">إيقاف الفلاش</string>')
add_to_file('app/src/main/res/values-es/strings.xml', '    <string name="action_flash_on">Encender flash</string>\n    <string name="action_flash_off">Apagar flash</string>')
