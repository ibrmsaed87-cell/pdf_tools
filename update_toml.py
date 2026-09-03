import re

with open('gradle/libs.versions.toml', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('[libraries]', 'appcompat = "1.6.1"\n\n[libraries]')
content = content.replace('[plugins]', 'androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }\n\n[plugins]')

with open('gradle/libs.versions.toml', 'w', encoding='utf-8') as f:
    f.write(content)
