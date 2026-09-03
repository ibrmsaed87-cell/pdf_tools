import os

files = [
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values-ar/strings.xml',
    'app/src/main/res/values-es/strings.xml'
]

for filepath in files:
    if os.path.exists(filepath):
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        content = content.replace('PRIVACY & ABOUT', 'PRIVACY &amp; ABOUT')
        
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

