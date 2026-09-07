import sys

filepath = 'app/build.gradle.kts'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = 'implementation(libs.androidx.appcompat)'
replacement = target + '\n    implementation(libs.pdfbox.android)'
content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched build")
