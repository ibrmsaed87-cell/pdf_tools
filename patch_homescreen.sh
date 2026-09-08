#!/bin/bash
awk '
/if \(tool.titleResId == R.string.action_image_to_pdf\)/ {
    print "                    if (tool.titleResId == R.string.action_create_pdf) {"
    print "                        onNavigateToCreatePdf()"
    print "                    } else if (tool.titleResId == R.string.action_image_to_pdf) {"
    print "                        onNavigateToImageToPdf()"
    next
}
{ print }
' app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt > app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt.tmp && mv app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt.tmp app/src/main/java/com/spinel/pdftools/ui/home/HomeScreen.kt
