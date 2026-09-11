import os

manifest_file = "app/src/main/AndroidManifest.xml"

with open(manifest_file, "r") as f:
    content = f.read()

target = """        <activity
            android:name=".MainActivity"
            android:windowSoftInputMode="adjustResize"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">"""

replacement = """        <activity
            android:name=".MainActivity"
            android:launchMode="singleTop"
            android:windowSoftInputMode="adjustResize"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">"""

if target in content:
    content = content.replace(target, replacement)
    with open(manifest_file, "w") as f:
        f.write(content)
    print("SUCCESS")
else:
    print("FAILED")
