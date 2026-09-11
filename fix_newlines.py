import re

def fix(path):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find the privacy_policy_content
    pattern = r'(<string name="privacy_policy_content">)(.*?)(</string>)'
    
    def replacer(match):
        # replace literal newlines with the string '\n'
        inner = match.group(2).replace('\n', r'\n')
        return match.group(1) + inner + match.group(3)
        
    updated = re.sub(pattern, replacer, content, flags=re.DOTALL)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(updated)

fix('app/src/main/res/values/strings.xml')
fix('app/src/main/res/values-ar/strings.xml')
fix('app/src/main/res/values-es/strings.xml')
