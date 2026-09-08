#!/bin/bash
awk '
BEGIN {
    skip = 0
}
/onNavigateToCreatePdf = \{/ {
    if (skip == 1) {
        getline
        getline
        skip = 0
        next
    }
    skip = 1
}
{ print }
' app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt > app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp && mv app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt.tmp app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt
