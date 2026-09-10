import os
import re

# 1. Update libs.versions.toml
toml_path = 'gradle/libs.versions.toml'
with open(toml_path, 'r', encoding='utf-8') as f:
    toml = f.read()

if 'playServicesAds' not in toml:
    toml = toml.replace('[versions]', '[versions]\nplayServicesAds = "23.3.0"\nuserMessagingPlatform = "3.0.0"')
    toml = toml.replace('[libraries]', '[libraries]\nplay-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }\nuser-messaging-platform = { group = "com.google.android.ump", name = "user-messaging-platform", version.ref = "userMessagingPlatform" }')
    with open(toml_path, 'w', encoding='utf-8') as f:
        f.write(toml)

# 2. Update app/build.gradle.kts
gradle_path = 'app/build.gradle.kts'
with open(gradle_path, 'r', encoding='utf-8') as f:
    gradle = f.read()

if 'libs.play.services.ads' not in gradle:
    gradle = gradle.replace('dependencies {', 'dependencies {\n  implementation(libs.play.services.ads)\n  implementation(libs.user.messaging.platform)')
    with open(gradle_path, 'w', encoding='utf-8') as f:
        f.write(gradle)

# 3. Update AndroidManifest.xml
manifest_path = 'app/src/main/AndroidManifest.xml'
with open(manifest_path, 'r', encoding='utf-8') as f:
    manifest = f.read()

if 'android.permission.INTERNET' not in manifest:
    manifest = manifest.replace('<uses-permission android:name="android.permission.CAMERA" />', '<uses-permission android:name="android.permission.CAMERA" />\n    <uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />')

if 'com.google.android.gms.ads.APPLICATION_ID' not in manifest:
    manifest = manifest.replace('<application', '<application')
    manifest = manifest.replace('android:theme="@style/Theme.MyApplication">', 'android:theme="@style/Theme.MyApplication">\n        <meta-data\n            android:name="com.google.android.gms.ads.APPLICATION_ID"\n            android:value="ca-app-pub-3940256099942544~3347511713"/>')
    with open(manifest_path, 'w', encoding='utf-8') as f:
        f.write(manifest)

# 4. Strings
strings_en = 'app/src/main/res/values/strings.xml'
with open(strings_en, 'r', encoding='utf-8') as f:
    s_en = f.read()
if 'setting_privacy_options' not in s_en:
    s_en = s_en.replace('</resources>', '    <string name="setting_privacy_options">Privacy options</string>\n</resources>')
    with open(strings_en, 'w', encoding='utf-8') as f:
        f.write(s_en)

strings_es = 'app/src/main/res/values-es/strings.xml'
if os.path.exists(strings_es):
    with open(strings_es, 'r', encoding='utf-8') as f:
        s_es = f.read()
    if 'setting_privacy_options' not in s_es:
        s_es = s_es.replace('</resources>', '    <string name="setting_privacy_options">Opciones de privacidad</string>\n</resources>')
        with open(strings_es, 'w', encoding='utf-8') as f:
            f.write(s_es)

strings_ar = 'app/src/main/res/values-ar/strings.xml'
if os.path.exists(strings_ar):
    with open(strings_ar, 'r', encoding='utf-8') as f:
        s_ar = f.read()
    if 'setting_privacy_options' not in s_ar:
        s_ar = s_ar.replace('</resources>', '    <string name="setting_privacy_options">خيارات الخصوصية</string>\n</resources>')
        with open(strings_ar, 'w', encoding='utf-8') as f:
            f.write(s_ar)
