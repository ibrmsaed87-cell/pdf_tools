import zipfile
import sys

apk_path = 'app/build/outputs/apk/debug/app-debug.apk'
with zipfile.ZipFile(apk_path, 'r') as apk:
    for name in apk.namelist():
        if name.endswith('.dex'):
            data = apk.read(name)
            if b'ImageToPdfScreenKt' in data:
                print(f"Found in {name}")
