import sys

filepath = 'app/src/main/java/com/spinel/pdftools/ui/files/FilesScreen.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

target_imports = """import androidx.compose.ui.unit.dp
import com.spinel.pdftools.R

@OptIn(ExperimentalMaterial3Api::class)"""

replacement_imports = """import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.spinel.pdftools.data.model.FileSource
import com.spinel.pdftools.data.model.PdfMetadata
import com.spinel.pdftools.R

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "Unknown size"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    if (digitGroups >= units.size) digitGroups = units.size - 1
    return java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

private fun formatDate(timestamp: Long): String {
    val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return formatter.format(java.util.Date(timestamp))
}

private fun openPdf(context: android.content.Context, uriString: String) {
    try {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_open_pdf)))
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.error_cannot_open_pdf), Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)"""

if target_imports in content:
    content = content.replace(target_imports, replacement_imports)
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched imports")
else:
    print("Target imports not found")
