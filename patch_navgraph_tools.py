import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """            composable(Screen.Tools.route) { 
                ToolsScreen(
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    }
                ) 
            }"""

replacement = """            composable(Screen.Tools.route) { 
                ToolsScreen(
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    },
                    onNavigateToScanDocument = {
                        navController.navigate(Screen.ScanDocument.route)
                    }
                ) 
            }"""

content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("PATCH APPLIED")
