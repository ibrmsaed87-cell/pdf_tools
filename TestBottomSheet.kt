import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    val allowDismiss = remember { mutableStateOf(false) }
    val state = rememberModalBottomSheetState(
        confirmValueChange = { 
            if (it == SheetValue.Hidden) allowDismiss.value else true 
        }
    )
}
