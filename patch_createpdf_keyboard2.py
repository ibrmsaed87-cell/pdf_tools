import re

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'r') as f:
    content = f.read()

bottom_sheet_old = """    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { if (it == androidx.compose.material3.SheetValue.Hidden) allowDismiss else true }
    )
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {"""

bottom_sheet_new = """    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { if (it == androidx.compose.material3.SheetValue.Hidden) allowDismiss else true }
    )
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    ModalBottomSheet(
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
                .verticalScroll(scrollState)
        ) {"""

content = content.replace(bottom_sheet_old, bottom_sheet_new)

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'w') as f:
    f.write(content)
