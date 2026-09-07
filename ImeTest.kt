import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.Composable

@Composable
fun TestIme() {
    val a = WindowInsets.isImeVisible
    val b = WindowInsets.ime.getBottom(LocalDensity.current) > 0
}
