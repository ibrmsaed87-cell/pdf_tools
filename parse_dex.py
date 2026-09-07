import zipfile

apk_path = 'app/build/outputs/apk/debug/app-debug.apk'
with zipfile.ZipFile(apk_path, 'r') as apk:
    for dex_name in apk.namelist():
        if not dex_name.endswith('.dex'): continue
        data = apk.read(dex_name)
        # very hacky way to find class definitions starting with Lcom/spinel/pdftools/ui/imagetopdf/
        import re
        matches = re.findall(b'Lcom/spinel/pdftools/ui/imagetopdf/[A-Za-z0-9_$]*;', data)
        for match in set(matches):
            if b'ImageToPdfScreenKt' in match:
                print(f"{dex_name}: {match.decode('utf-8')}")
