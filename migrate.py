import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

# 1. NavGraph
def migrate_navgraph(content):
    content = content.replace(
        "ImageToPdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "ImageToPdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    content = content.replace(
        "ScanDocumentScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "ScanDocumentScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    content = content.replace(
        "MergePdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "MergePdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    content = content.replace(
        "SplitPdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "SplitPdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    content = content.replace(
        "CompressPdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "CompressPdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    content = content.replace(
        "OrganizePdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )",
        "OrganizePdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )"
    )
    return content
update_file('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', migrate_navgraph)

# 2. ImageToPdf
def migrate_imagetopdf(content):
    content = content.replace(
        "fun ImageToPdfScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: ImageToPdfViewModel = viewModel()\n)",
        "fun ImageToPdfScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: ImageToPdfViewModel = viewModel()\n)"
    )
    old_btn = """                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(state.outputUri, "application/pdf")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },"""
    new_btn = """                                Button(
                                    onClick = { onNavigateToViewer(state.outputUri.toString()) },"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt', migrate_imagetopdf)

# 3. ScanDocument
def migrate_scandocument(content):
    content = content.replace(
        "fun ScanDocumentScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: ScanDocumentViewModel = viewModel()\n)",
        "fun ScanDocumentScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: ScanDocumentViewModel = viewModel()\n)"
    )
    old_btn = """            Button(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },"""
    new_btn = """            Button(
                onClick = { onNavigateToViewer(uri.toString()) },"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt', migrate_scandocument)

# 4. CompressPdf
def migrate_compresspdf(content):
    content = content.replace(
        "fun CompressPdfScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: CompressPdfViewModel = viewModel()\n)",
        "fun CompressPdfScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: CompressPdfViewModel = viewModel()\n)"
    )
    old_btn = """                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(currentState.uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_open_pdf)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, R.string.error_cannot_open_pdf, Toast.LENGTH_SHORT).show()
                                }
                            },"""
    new_btn = """                        Button(
                            onClick = { onNavigateToViewer(currentState.uri.toString()) },"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt', migrate_compresspdf)

# 5. SplitPdf
def migrate_splitpdf(content):
    content = content.replace(
        "fun SplitPdfScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: SplitPdfViewModel = viewModel()\n)",
        "fun SplitPdfScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: SplitPdfViewModel = viewModel()\n)"
    )
    old_btn = """                        onOpen = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(currentState.savedUri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(intent, "Open PDF"))
                            } catch (e: Exception) {
                                // No app found
                            }
                            viewModel.reset()
                        }"""
    new_btn = """                        onOpen = {
                            onNavigateToViewer(currentState.savedUri.toString())
                            viewModel.reset()
                        }"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/splitpdf/SplitPdfScreen.kt', migrate_splitpdf)

# 6. MergePdf
def migrate_mergepdf(content):
    content = content.replace(
        "fun MergePdfScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: MergePdfViewModel = viewModel()\n)",
        "fun MergePdfScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: MergePdfViewModel = viewModel()\n)"
    )
    old_btn = """                        onOpen = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(currentState.savedUri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(intent, "Open PDF"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
                            }
                            viewModel.reset()
                        }"""
    new_btn = """                        onOpen = {
                            onNavigateToViewer(currentState.savedUri.toString())
                            viewModel.reset()
                        }"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/mergepdf/MergePdfScreen.kt', migrate_mergepdf)

# 7. OrganizePdf
def migrate_organizepdf(content):
    content = content.replace(
        "fun OrganizePdfScreen(\n    onNavigateBack: () -> Unit,\n    viewModel: OrganizePdfViewModel = viewModel()\n)",
        "fun OrganizePdfScreen(\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {},\n    viewModel: OrganizePdfViewModel = viewModel()\n)"
    )
    old_btn = """                        onOpen = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(currentState.savedUri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open PDF"))
                        }"""
    new_btn = """                        onOpen = {
                            onNavigateToViewer(currentState.savedUri.toString())
                        }"""
    return content.replace(old_btn, new_btn)
update_file('app/src/main/java/com/spinel/pdftools/ui/organizepdf/OrganizePdfScreen.kt', migrate_organizepdf)

