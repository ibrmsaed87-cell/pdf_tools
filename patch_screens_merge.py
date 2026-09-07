import sys

def patch_file(filepath, replace_dict):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    for t, r in replace_dict.items():
        content = content.replace(t, r)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

# HomeScreen.kt
patch_file('app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt', {
    'onNavigateToScanDocument: () -> Unit = {}': 'onNavigateToScanDocument: () -> Unit = {}, onNavigateToMergePdf: () -> Unit = {}',
    'if (tool.titleResId == R.string.action_image_to_pdf) {\n                            onNavigateToImageToPdf()\n                        } else if (tool.titleResId == R.string.action_scan_document) {\n                            onNavigateToScanDocument()\n                        } else {\n                            /* Coming soon */\n                        }': 'if (tool.titleResId == R.string.action_image_to_pdf) {\n                            onNavigateToImageToPdf()\n                        } else if (tool.titleResId == R.string.action_scan_document) {\n                            onNavigateToScanDocument()\n                        } else if (tool.titleResId == R.string.action_merge_pdf) {\n                            onNavigateToMergePdf()\n                        } else {\n                            /* Coming soon */\n                        }'
})

# ToolsScreen.kt
patch_file('app/src/main/java/com/spinel/pdftools/ui/tools/ToolsScreen.kt', {
    'onNavigateToScanDocument: () -> Unit = {}': 'onNavigateToScanDocument: () -> Unit = {}, onNavigateToMergePdf: () -> Unit = {}',
    'if (tool.titleResId == R.string.action_image_to_pdf) {\n                            onNavigateToImageToPdf()\n                        } else if (tool.titleResId == R.string.action_scan_document) {\n                            onNavigateToScanDocument()\n                        } else {\n                            /* Coming soon */\n                        }': 'if (tool.titleResId == R.string.action_image_to_pdf) {\n                            onNavigateToImageToPdf()\n                        } else if (tool.titleResId == R.string.action_scan_document) {\n                            onNavigateToScanDocument()\n                        } else if (tool.titleResId == R.string.action_merge_pdf) {\n                            onNavigateToMergePdf()\n                        } else {\n                            /* Coming soon */\n                        }'
})

print("HomeScreen and ToolsScreen patched")
