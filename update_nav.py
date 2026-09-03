with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'r', encoding='utf-8') as f:
    content = f.read()

replacement = """
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToTools = {
                        navController.navigate(Screen.Tools.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
"""

content = content.replace("composable(Screen.Home.route) { HomeScreen() }", replacement.strip())

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'w', encoding='utf-8') as f:
    f.write(content)
