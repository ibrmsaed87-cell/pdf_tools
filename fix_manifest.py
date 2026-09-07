import sys

filepath = 'app/src/main/AndroidManifest.xml'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'android:name=".MainActivity"'
replacement = 'android:name=".MainActivity"\n            android:windowSoftInputMode="adjustResize"'

if target in content and 'adjustResize' not in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched AndroidManifest.xml")
elif 'adjustResize' in content:
    print("Already has adjustResize")
else:
    print("Target not found")
