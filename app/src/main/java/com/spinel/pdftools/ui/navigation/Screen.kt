package com.spinel.pdftools.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.spinel.pdftools.R

sealed class Screen(val route: String, @StringRes val labelResId: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Filled.Home)
    object Files : Screen("files", R.string.nav_files, Icons.Filled.Folder)
    object Tools : Screen("tools", R.string.nav_tools, Icons.Filled.Build)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}
