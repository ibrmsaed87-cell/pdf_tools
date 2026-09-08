import re

def update_file(path, func):
    with open(path, 'r') as f:
        content = f.read()
    new_content = func(content)
    with open(path, 'w') as f:
        f.write(new_content)

def fix_scandoc(content):
    # Pass onNavigateToViewer to ScanSuccessView
    content = content.replace(
        "ScanSuccessView(\n                            uri = scanState.uri,\n                            onDone = { onNavigateBack() }\n                        )",
        "ScanSuccessView(\n                            uri = scanState.uri,\n                            onDone = { onNavigateBack() },\n                            onNavigateToViewer = onNavigateToViewer\n                        )"
    )
    # Add parameter to ScanSuccessView
    content = content.replace(
        "fun ScanSuccessView(\n    uri: Uri,\n    onDone: () -> Unit\n)",
        "fun ScanSuccessView(\n    uri: Uri,\n    onDone: () -> Unit,\n    onNavigateToViewer: (String) -> Unit\n)"
    )
    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/scandocument/ScanDocumentScreen.kt', fix_scandoc)

def fix_compresspdf(content):
    # Add onNavigateToViewer to CompressPdfScreen signature
    content = content.replace(
        "fun CompressPdfScreen(\n    viewModel: CompressPdfViewModel = viewModel(),\n    onNavigateBack: () -> Unit\n)",
        "fun CompressPdfScreen(\n    viewModel: CompressPdfViewModel = viewModel(),\n    onNavigateBack: () -> Unit,\n    onNavigateToViewer: (String) -> Unit = {}\n)"
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
    content = content.replace(old_btn, new_btn)
    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/compresspdf/CompressPdfScreen.kt', fix_compresspdf)

def fix_navgraph(content):
    content = content.replace(
        "composable(Screen.CompressPdf.route) { \n                com.spinel.pdftools.ui.compresspdf.CompressPdfScreen(\n                    onNavigateBack = { navController.popBackStack() }\n                )\n            }",
        "composable(Screen.CompressPdf.route) { \n                com.spinel.pdftools.ui.compresspdf.CompressPdfScreen(\n                    onNavigateBack = { navController.popBackStack() },\n                    onNavigateToViewer = { uri -> navController.navigate(Screen.PdfViewer.createRoute(uri)) }\n                )\n            }"
    )
    return content

update_file('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', fix_navgraph)

