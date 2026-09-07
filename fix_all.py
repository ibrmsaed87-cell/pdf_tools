import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                    onNavigateToMergePdf = {
                        navController.navigate(Screen.MergePdf.route)
                    },
                    onNavigateToSplitPdf = {
                        navController.navigate(Screen.SplitPdf.route)
                    },
                    onNavigateToMergePdf = {
                        navController.navigate(Screen.MergePdf.route)
                    },
                    onNavigateToSplitPdf = {
                        navController.navigate(Screen.SplitPdf.route)
                    }'''
replacement = '''                    onNavigateToMergePdf = {
                        navController.navigate(Screen.MergePdf.route)
                    },
                    onNavigateToSplitPdf = {
                        navController.navigate(Screen.SplitPdf.route)
                    }'''
content = content.replace(target, replacement)

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

vmpath = 'app/src/main/java/com/spinel/pdftools/ui/splitpdf/SplitPdfViewModel.kt'
with open(vmpath, 'r', encoding='utf-8') as f:
    vm_content = f.read()

vm_content = vm_content.replace('com.tomroush', 'com.tom_roush')
with open(vmpath, 'w', encoding='utf-8') as f:
    f.write(vm_content)
print("Fixed both issues.")
