import sys
import xml.etree.ElementTree as ET

def add_strings(file_path, new_strings):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        existing_names = set([elem.attrib['name'] for elem in root.findall('string')])
        
        changed = False
        for name, value in new_strings.items():
            if name not in existing_names:
                new_elem = ET.SubElement(root, 'string', {'name': name})
                new_elem.text = value
                changed = True
                
        if changed:
            ET.indent(tree, space="    ", level=0)
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            print(f"Updated {file_path}")
    except Exception as e:
        print(f"Error processing {file_path}: {e}")

strings_en = {
    "action_enter_fullscreen": "Enter fullscreen",
    "action_exit_fullscreen": "Exit fullscreen"
}

strings_ar = {
    "action_enter_fullscreen": "ملء الشاشة",
    "action_exit_fullscreen": "إنهاء ملء الشاشة"
}

strings_es = {
    "action_enter_fullscreen": "Pantalla completa",
    "action_exit_fullscreen": "Salir de pantalla completa"
}

add_strings('app/src/main/res/values/strings.xml', strings_en)
add_strings('app/src/main/res/values-ar/strings.xml', strings_ar)
add_strings('app/src/main/res/values-es/strings.xml', strings_es)
