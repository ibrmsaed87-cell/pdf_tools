import re

def add_strings(filepath, new_strings):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    insert_str = ""
    for k, v in new_strings.items():
        if f'<string name="{k}">' not in content:
            insert_str += f'    <string name="{k}">{v}</string>\n'
            
    if insert_str:
        content = content.replace('</resources>', f'{insert_str}</resources>')
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

en = {
    'files_subtitle': 'Your documents in one place',
    'filter_recent': 'Recent',
    'filter_created': 'Created',
    'filter_opened': 'Opened',
    'empty_files_title': 'No files yet',
    'empty_files_desc': 'Files you create or open will appear here.',
    'action_open_pdf': 'Open a PDF',
    'action_share': 'Share',
    'action_rename': 'Rename',
    'action_delete': 'Delete',
    'action_open': 'Open'
}

ar = {
    'files_subtitle': 'مستنداتك في مكان واحد',
    'filter_recent': 'الأحدث',
    'filter_created': 'تم إنشاؤها',
    'filter_opened': 'تم فتحها',
    'empty_files_title': 'لا توجد ملفات بعد',
    'empty_files_desc': 'الملفات التي تقوم بإنشائها أو فتحها ستظهر هنا.',
    'action_open_pdf': 'فتح ملف PDF',
    'action_share': 'مشاركة',
    'action_rename': 'إعادة تسمية',
    'action_delete': 'حذف',
    'action_open': 'فتح'
}

es = {
    'files_subtitle': 'Tus documentos en un solo lugar',
    'filter_recent': 'Recientes',
    'filter_created': 'Creados',
    'filter_opened': 'Abiertos',
    'empty_files_title': 'Aún no hay archivos',
    'empty_files_desc': 'Los archivos que crees o abras aparecerán aquí.',
    'action_open_pdf': 'Abrir un PDF',
    'action_share': 'Compartir',
    'action_rename': 'Renombrar',
    'action_delete': 'Eliminar',
    'action_open': 'Abrir'
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
