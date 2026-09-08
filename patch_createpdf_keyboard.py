import re

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
"""
content = content.replace('import androidx.compose.ui.graphics.Color\n', 'import androidx.compose.ui.graphics.Color\n' + imports_to_add)


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


save_btn_old = """                Button(
                    onClick = {
                        if (title.isBlank() && body.isBlank()) {
                            isError = true
                        } else {
                            allowDismiss = true
                            scope.launch {
                                sheetState.hide()
                                onSave(title, body, titleStyle, bodyStyle)
                            }
                        }
                    }
                ) {"""

save_btn_new = """                Button(
                    onClick = {
                        if (title.isBlank() && body.isBlank()) {
                            isError = true
                        } else {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            allowDismiss = true
                            scope.launch {
                                sheetState.hide()
                                onSave(title, body, titleStyle, bodyStyle)
                            }
                        }
                    }
                ) {"""

content = content.replace(save_btn_old, save_btn_new)

cancel_btn_old = """                TextButton(onClick = {
                    allowDismiss = true
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }) {"""

cancel_btn_new = """                TextButton(onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    allowDismiss = true
                    scope.launch {
                        sheetState.hide()
                        onDismiss()
                    }
                }) {"""

content = content.replace(cancel_btn_old, cancel_btn_new)

with open('app/src/main/java/com/spinel/pdftools/ui/createpdf/CreatePdfScreen.kt', 'w') as f:
    f.write(content)
