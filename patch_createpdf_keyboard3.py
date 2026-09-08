import re

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'r') as f:
    content = f.read()

pattern = r"ModalBottomSheet\(\s*onDismissRequest = onDismiss,\s*sheetState = sheetState,\s*\)\s*\{\s*Column\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.padding\(horizontal = 16\.dp, vertical = 8\.dp\)\s*\)\s*\{"

replacement = """ModalBottomSheet(
        onDismissRequest = {
            if (allowDismiss) onDismiss()
        },
        sheetState = sheetState,
        properties = androidx.compose.material3.ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        )
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
        val isImeVisible = imeBottom > 0
        
        BackHandler {
            if (isImeVisible) {
                keyboardController?.hide()
                focusManager.clearFocus()
            } else {
                allowDismiss = true
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {"""

content = re.sub(pattern, replacement, content, flags=re.MULTILINE)

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'w') as f:
    f.write(content)
