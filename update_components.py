import re

with open('app/src/main/java/com/spinel/pdftools/ui/components/PremiumComponents.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Make PremiumEmptyState more compact
content = content.replace(
    'modifier = modifier\n            .fillMaxWidth()\n            .padding(32.dp),',
    'modifier = modifier\n            .fillMaxWidth()\n            .padding(24.dp),'
)
content = content.replace(
    '.size(72.dp)',
    '.size(48.dp)'
)
content = content.replace(
    '.size(32.dp)',
    '.size(24.dp)'
)
content = content.replace(
    'Spacer(modifier = Modifier.height(24.dp))',
    'Spacer(modifier = Modifier.height(16.dp))'
)
content = content.replace(
    'Spacer(modifier = Modifier.height(8.dp))',
    'Spacer(modifier = Modifier.height(4.dp))'
)

new_component = """
@Composable
fun PremiumGridToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconContainerColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
"""

if "fun PremiumGridToolCard" not in content:
    content += new_component

with open('app/src/main/java/com/spinel/pdftools/ui/components/PremiumComponents.kt', 'w', encoding='utf-8') as f:
    f.write(content)
