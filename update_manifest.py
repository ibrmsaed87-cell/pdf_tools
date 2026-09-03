import re

with open('app/src/main/AndroidManifest.xml', 'r', encoding='utf-8') as f:
    content = f.read()

app_tag_replacement = """    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:localeConfig="@xml/locales_config"
        android:theme="@style/Theme.MyApplication">
        
        <service
            android:name="androidx.appcompat.app.AppLocalesMetadataHolderService"
            android:enabled="false"
            android:exported="false">
            <meta-data
                android:name="autoStoreLocales"
                android:value="true" />
        </service>"""

content = re.sub(
    r'<application.*?:theme="@style/Theme\.MyApplication">',
    app_tag_replacement,
    content,
    flags=re.DOTALL
)

with open('app/src/main/AndroidManifest.xml', 'w', encoding='utf-8') as f:
    f.write(content)
