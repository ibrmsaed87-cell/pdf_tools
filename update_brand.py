import os
import re

def update_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

# English
en_replacements = [
    ('>PDF Tools<', '>Docvra<'),
    ('PDF Tools processes', 'Docvra processes'),
    ('PDF Tools.', 'Docvra.')
]
update_file('app/src/main/res/values/strings.xml', en_replacements)

# Spanish
es_replacements = [
    ('>PDF Tools<', '>Docvra<'),
    ('PDF Tools procesa', 'Docvra procesa'),
    ('PDF Tools.', 'Docvra.')
]
update_file('app/src/main/res/values-es/strings.xml', es_replacements)

# Arabic
ar_replacements = [
    ('>أدوات PDF<', '>Docvra<'),
    ('تقوم أدوات PDF بمعالجة', 'تقوم Docvra بمعالجة'),
    ('أدوات PDF.', 'Docvra.')
]
update_file('app/src/main/res/values-ar/strings.xml', ar_replacements)

# Test
test_replacements = [
    ('"PDF Tools"', '"Docvra"')
]
update_file('app/src/test/java/com/spinel/pdftools/ExampleRobolectricTest.kt', test_replacements)

print("Done")
