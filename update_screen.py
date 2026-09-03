with open('app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I will add PrivacyPolicy and About objects. I can reuse the Settings icon or another if needed, but they aren't shown in the bottom bar anyway.
insert = """    object PrivacyPolicy : Screen("privacy_policy", R.string.privacy_policy, Icons.Filled.Settings)
    object About : Screen("about", R.string.setting_about, Icons.Filled.Settings)
}"""

content = content.replace('}', insert)

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt', 'w', encoding='utf-8') as f:
    f.write(content)
