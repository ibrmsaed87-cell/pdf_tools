import re

with open('app/src/main/java/com/spinel/pdftools/MainActivity.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import androidx.activity.ComponentActivity', 'import androidx.appcompat.app.AppCompatActivity')
content = content.replace('class MainActivity : ComponentActivity()', 'class MainActivity : AppCompatActivity()')

with open('app/src/main/java/com/spinel/pdftools/MainActivity.kt', 'w', encoding='utf-8') as f:
    f.write(content)
