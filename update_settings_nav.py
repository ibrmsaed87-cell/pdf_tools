import re

with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Modify the signature
content = content.replace('fun SettingsScreen() {', 'fun SettingsScreen(onNavigateToPrivacy: () -> Unit = {}, onNavigateToAbout: () -> Unit = {}) {')

# Modify the Privacy Policy onClick
# Find: onClick = { /* Coming soon */ } after stringResource(id = R.string.privacy_policy)
privacy_block = re.search(r'(title = stringResource\(id = R\.string\.privacy_policy\).*?onClick = )\{ /\* Coming soon \*/ \}', content, re.DOTALL)
if privacy_block:
    content = content[:privacy_block.start()] + privacy_block.group(1) + 'onNavigateToPrivacy' + content[privacy_block.end():]

about_block = re.search(r'(title = stringResource\(id = R\.string\.setting_about\).*?onClick = )\{ /\* Coming soon \*/ \}', content, re.DOTALL)
if about_block:
    content = content[:about_block.start()] + about_block.group(1) + 'onNavigateToAbout' + content[about_block.end():]

with open('app/src/main/java/com/spinel/pdftools/ui/settings/SettingsScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

