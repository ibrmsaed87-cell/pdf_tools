import os

def add_strings(filepath, new_strings):
    if not os.path.exists(filepath): return
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
    "about_version": "Version %1$s",
    "about_copyright": "© 2026 PDF Tools. All rights reserved.",
    "privacy_policy_content": "PDF Tools processes your documents locally. Your files remain on your device and under your control. We use Android\\'s Storage Access Framework to securely access files you select. No documents are uploaded to our servers. We do not collect analytics or advertising data. This policy may be updated in the future if new features require additional disclosure."
}

ar = {
    "about_version": "الإصدار %1$s",
    "about_copyright": "© 2026 أدوات PDF. جميع الحقوق محفوظة.",
    "privacy_policy_content": "تقوم أدوات PDF بمعالجة مستنداتك محليًا. تبقى ملفاتك على جهازك وتحت سيطرتك. نحن نستخدم إطار عمل الوصول إلى التخزين الخاص بنظام أندرويد للوصول الآمن إلى الملفات التي تختارها. لا يتم رفع أي مستندات إلى خوادمنا. نحن لا نجمع بيانات التحليلات أو الإعلانات. قد يتم تحديث هذه السياسة في المستقبل إذا تطلبت الميزات الجديدة إفصاحًا إضافيًا."
}

es = {
    "about_version": "Versión %1$s",
    "about_copyright": "© 2026 PDF Tools. Todos los derechos reservados.",
    "privacy_policy_content": "PDF Tools procesa sus documentos localmente. Sus archivos permanecen en su dispositivo y bajo su control. Utilizamos el Storage Access Framework de Android para acceder de forma segura a los archivos que seleccione. Ningún documento se carga en nuestros servidores. No recopilamos datos de análisis ni de publicidad. Esta política podría actualizarse en el futuro si nuevas funciones requieren una divulgación adicional."
}

add_strings('app/src/main/res/values/strings.xml', en)
add_strings('app/src/main/res/values-ar/strings.xml', ar)
add_strings('app/src/main/res/values-es/strings.xml', es)
