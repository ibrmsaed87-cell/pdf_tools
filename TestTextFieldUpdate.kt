import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.singleWindowApplication

fun main() = singleWindowApplication {
    var size by remember { mutableStateOf(20f) }
    var text by remember { mutableStateOf("Test") }
    
    Column {
        Button(onClick = { size += 5f }) { Text("Increase") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = LocalTextStyle.current.copy(fontSize = size.sp)
        )
    }
}
