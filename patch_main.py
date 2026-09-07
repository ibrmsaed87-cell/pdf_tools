import sys

filepath = 'app/src/main/java/com/spinel/pdftools/MainActivity.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('import androidx.appcompat.app.AppCompatActivity', 'import androidx.appcompat.app.AppCompatActivity\nimport com.tomroush.pdfbox.android.PDFBoxResourceLoader')
content = content.replace('super.onCreate(savedInstanceState)\n    enableEdgeToEdge()', 'super.onCreate(savedInstanceState)\n    PDFBoxResourceLoader.init(applicationContext)\n    enableEdgeToEdge()')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched main")
