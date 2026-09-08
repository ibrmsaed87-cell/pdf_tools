#!/bin/bash
awk '
/composable\(Screen.ImageToPdf.route\)/ {
    print "            composable(Screen.CreatePdf.route) { "
    print "                CreatePdfScreen("
    print "                    onNavigateBack = { navController.popBackStack() },"
    print "                    onNavigateToViewer = { uri -> "
    print "                        navController.navigate(Screen.PdfViewer.createRoute(uri))"
    print "                    }"
    print "                )"
    print "            }"
}
{ print }
' app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt > app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp && mv app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt
