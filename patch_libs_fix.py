import sys

filepath = 'gradle/libs.versions.toml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('group = "com.tomroush"', 'group = "com.tom-roush"')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched libs fix")
