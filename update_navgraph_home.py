import re

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = r'composable\(Screen\.Home\.route\) \{.*?\) \{.*?\}\s*\}'
target2 = r'composable\(Screen\.Home\.route\) \{[\s\S]*?\}             \}'

# The simplest is to find composable(Screen.Home.route) and the next composable
start = content.find('composable(Screen.Home.route)')
end = content.find('composable(Screen.Files.route)')

new_block = """composable(Screen.Home.route) { 
                HomeScreen(
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
                ) 
            }
            """

content = content[:start] + new_block + content[end:]

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'w', encoding='utf-8') as f:
    f.write(content)
