import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/imagetopdf/ImageToPdfScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target = """    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Title, 1 = Body

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
    )

    val scrollState = rememberScrollState()
    
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val imeBottom = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current)
    val isImeVisible = imeBottom > 0

    androidx.activity.compose.BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {"""

replacement = """    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Title, 1 = Body

    var allowDismiss by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
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
        containerColor = MaterialTheme.colorScheme.surface,
        properties = androidx.compose.material3.ModalBottomSheetProperties(
            shouldDismissOnBackPress = false
        )
    ) {
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val imeBottom = androidx.compose.foundation.layout.WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current)
        val isImeVisible = imeBottom > 0

        androidx.activity.compose.BackHandler {
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
"""

if target in content:
    content = content.replace(target, replacement)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched TextEditorBottomSheet logic")
else:
    print("Target not found")
