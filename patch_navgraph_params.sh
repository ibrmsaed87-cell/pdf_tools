#!/bin/bash
awk '
/onNavigateToImageToPdf = \{/ {
    if (!done_home && in_home) {
        print "                    onNavigateToCreatePdf = {"
        print "                        navController.navigate(Screen.CreatePdf.route)"
        print "                    },"
        done_home = 1
    }
    if (!done_tools && in_tools) {
        print "                    onNavigateToCreatePdf = {"
        print "                        navController.navigate(Screen.CreatePdf.route)"
        print "                    },"
        done_tools = 1
    }
}
/composable\(Screen.Home.route\)/ { in_home = 1 }
/composable\(Screen.Tools.route\)/ { in_tools = 1; in_home = 0 }
/composable\(Screen.Settings.route\)/ { in_tools = 0 }
{ print }
' app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt > app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp && mv app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt
