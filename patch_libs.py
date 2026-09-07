import sys

filepath = 'gradle/libs.versions.toml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('appcompat = "1.6.1"\n\n[libraries]', 'appcompat = "1.6.1"\npdfbox = "2.0.27.0"\n\n[libraries]')
content = content.replace('[plugins]', 'pdfbox-android = { group = "com.tomroush", name = "pdfbox-android", version.ref = "pdfbox" }\n\n[plugins]')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched libs")
