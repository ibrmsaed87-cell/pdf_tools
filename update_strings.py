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
    'settings_subtitle': 'Customize your experience',
    'section_preferences': 'PREFERENCES',
    'section_privacy_about': 'PRIVACY & ABOUT',
    'desc_app_theme': 'App theme',
    'desc_app_language': 'App language',
    'lang_english': 'English',
    'lang_arabic': 'العربية',
    'lang_spanish': 'Español',
    'privacy_policy': 'Privacy Policy'
}

ar = {
    'settings_subtitle': 'تخصيص تجربتك',
    'section_preferences': 'التفضيلات',
    'section_privacy_about': 'الخصوصية وحول التطبيق',
    'desc_app_theme': 'مظهر التطبيق',
    'desc_app_language': 'لغة التطبيق',
    'lang_english': 'English',
    'lang_arabic': 'العربية',
    'lang_spanish': 'Español',
    'privacy_policy': 'سياسة الخصوصية'
}

es = {
    'settings_subtitle': 'Personaliza tu experiencia',
    'section_preferences': 'PREFERENCIAS',
    'section_privacy_about': 'PRIVACIDAD Y ACERCA DE',
    'desc_app_theme': 'Tema de la app',
    'desc_app_language': 'Idioma de la app',
    'lang_english': 'English',
    'lang_arabic': 'العربية',
    'lang_spanish': 'Español',
    'privacy_policy': 'Política de privacidad'
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
