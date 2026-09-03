with open('app/src/main/java/com/spinel/pdftools/ui/components/PremiumComponents.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Remove padding from SectionHeader
content = content.replace(
    'modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),',
    'modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),'
)

# Remove padding from PremiumPrimaryCard
content = content.replace(
    'modifier = modifier\n            .fillMaxWidth()\n            .padding(horizontal = 16.dp),',
    'modifier = modifier.fillMaxWidth(),'
)

with open('app/src/main/java/com/spinel/pdftools/ui/components/PremiumComponents.kt', 'w', encoding='utf-8') as f:
    f.write(content)
