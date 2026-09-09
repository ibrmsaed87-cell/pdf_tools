import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

def fix_language(content):
    old = 'val currentLanguage = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else "en"'
    new = 'val currentLanguage = if (!currentLocales.isEmpty) currentLocales.get(0)?.language ?: "en" else context.resources.configuration.locales.get(0).language'
    return content.replace(old, new)

update_file('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', fix_language)
