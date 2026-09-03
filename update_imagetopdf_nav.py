with open('app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

insert = """    object PrivacyPolicy : Screen("privacy_policy", R.string.privacy_policy, Icons.Filled.Settings)
    object About : Screen("about", R.string.setting_about, Icons.Filled.Settings)
    object ImageToPdf : Screen("image_to_pdf", R.string.title_image_to_pdf, Icons.Filled.Image)
}"""

# Replace the end
content = content[:content.rfind('    object PrivacyPolicy')] + insert

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/Screen.kt', 'w', encoding='utf-8') as f:
    f.write(content)


with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add import
imports = """import com.spinel.pdftools.ui.about.AboutScreen
import com.spinel.pdftools.ui.about.PrivacyPolicyScreen
import com.spinel.pdftools.ui.imagetopdf.ImageToPdfScreen
"""
content = content.replace('import com.spinel.pdftools.ui.about.AboutScreen\nimport com.spinel.pdftools.ui.about.PrivacyPolicyScreen\n', imports)

# Add route
routes_start = content.find('composable(Screen.About.route)')
routes_end = content.find('}', routes_start)
# find the closing brace of the composable
bracket_count = 1
i = content.find('{', routes_start) + 1
while bracket_count > 0 and i < len(content):
    if content[i] == '{': bracket_count += 1
    elif content[i] == '}': bracket_count -= 1
    i += 1

routes_replacement = """composable(Screen.About.route) { 
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
                ) 
            }
            composable(Screen.ImageToPdf.route) { 
                ImageToPdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }"""

content = content[:routes_start] + routes_replacement + content[i:]

# update Home action
home_block = content.find('HomeScreen(')
content = content[:home_block] + """HomeScreen(
                    onNavigateToTools = {
                        navController.navigate(Screen.Tools.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    }
                )""" + content[content.find(')', home_block) + 1:]

# update Tools action
tools_block = content.find('composable(Screen.Tools.route) { ToolsScreen() }')
content = content.replace('composable(Screen.Tools.route) { ToolsScreen() }', """composable(Screen.Tools.route) { 
                ToolsScreen(
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    }
                ) 
            }""")

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'w', encoding='utf-8') as f:
    f.write(content)
