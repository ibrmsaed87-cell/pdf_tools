package com.spinel.pdftools.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.GridView
import androidx.compose.ui.graphics.vector.ImageVector
import com.spinel.pdftools.R

sealed class Screen(val route: String, val labelResId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    object Files : Screen("files", R.string.nav_files, Icons.Filled.Folder)
    object Tools : Screen("tools", R.string.nav_tools, Icons.Filled.Build)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
    object PrivacyPolicy : Screen("privacy_policy", R.string.privacy_policy, Icons.Filled.Settings)
    object About : Screen("about", R.string.setting_about, Icons.Filled.Settings)
    object ImageToPdf : Screen("image_to_pdf", R.string.title_image_to_pdf, Icons.Filled.Image)
    object ScanDocument : Screen("scan_document", R.string.title_scan_document, Icons.Filled.DocumentScanner)
    object MergePdf : Screen("merge_pdf", R.string.title_merge_pdf, Icons.AutoMirrored.Filled.MergeType)
    object SplitPdf : Screen("split_pdf", R.string.title_split_pdf, Icons.Filled.ContentCut)
    object CompressPdf : Screen("compress_pdf", R.string.title_compress_pdf, Icons.Filled.Build)
    object OrganizePdf : Screen("organize_pdf", R.string.title_organize_pdf, Icons.Filled.GridView)
}
