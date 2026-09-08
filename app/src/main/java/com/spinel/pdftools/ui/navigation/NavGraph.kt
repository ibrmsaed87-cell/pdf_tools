package com.spinel.pdftools.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spinel.pdftools.ui.files.FilesScreen
import com.spinel.pdftools.ui.home.HomeScreen
import com.spinel.pdftools.ui.settings.SettingsScreen
import com.spinel.pdftools.ui.about.AboutScreen
import com.spinel.pdftools.ui.about.PrivacyPolicyScreen
import com.spinel.pdftools.ui.imagetopdf.ImageToPdfScreen
import com.spinel.pdftools.ui.scandocument.ScanDocumentScreen
import com.spinel.pdftools.ui.tools.ToolsScreen
import com.spinel.pdftools.ui.mergepdf.MergePdfScreen
import com.spinel.pdftools.ui.splitpdf.SplitPdfScreen
import com.spinel.pdftools.ui.organizepdf.OrganizePdfScreen
import com.spinel.pdftools.ui.pdftojpg.PdfToJpgScreen
import com.spinel.pdftools.ui.viewer.PdfViewerScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Files,
        Screen.Tools,
        Screen.Settings
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val isMainScreen = items.any { it.route == currentDestination?.route }
            
            if (isMainScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = stringResource(screen.labelResId)) },
                            label = { Text(stringResource(screen.labelResId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    onNavigateToTools = {
                        navController.navigate(Screen.Tools.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    },
                    onNavigateToScanDocument = {
                        navController.navigate(Screen.ScanDocument.route)
                    },
                    onNavigateToMergePdf = {
                        navController.navigate(Screen.MergePdf.route)
                    },
                    onNavigateToSplitPdf = {
                        navController.navigate(Screen.SplitPdf.route)
                    },
                    onNavigateToCompressPdf = {
                        navController.navigate(Screen.CompressPdf.route)
                    },
                    onNavigateToOrganizePdf = {
                        navController.navigate(Screen.OrganizePdf.route)
                    },
                    onNavigateToPdfToJpg = {
                        navController.navigate(Screen.PdfToJpg.route)
                        
                    }
                ) 
            }
            composable(Screen.Files.route) { 
                FilesScreen(
                    onNavigateToPdfViewer = { uri ->
                        navController.navigate(Screen.PdfViewer.createRoute(uri))
                    }
                ) 
            }
            composable(Screen.Tools.route) { 
                ToolsScreen(
                    onNavigateToImageToPdf = {
                        navController.navigate(Screen.ImageToPdf.route)
                    },
                    onNavigateToScanDocument = {
                        navController.navigate(Screen.ScanDocument.route)
                    },
                    onNavigateToMergePdf = {
                        navController.navigate(Screen.MergePdf.route)
                    },
                    onNavigateToSplitPdf = {
                        navController.navigate(Screen.SplitPdf.route)
                    },
                    onNavigateToCompressPdf = {
                        navController.navigate(Screen.CompressPdf.route)
                    },
                    onNavigateToOrganizePdf = {
                        navController.navigate(Screen.OrganizePdf.route)
                    },
                    onNavigateToPdfToJpg = {
                        navController.navigate(Screen.PdfToJpg.route)
                        
                    }
                ) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                ) 
            }
            composable(Screen.PrivacyPolicy.route) { 
                PrivacyPolicyScreen(onNavigateBack = { navController.popBackStack() }) 
            }
            composable(Screen.About.route) { 
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) }
                ) 
            }
            composable(Screen.ImageToPdf.route) { 
                ImageToPdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ScanDocument.route) { 
                ScanDocumentScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.MergePdf.route) { 
                MergePdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SplitPdf.route) { 
                SplitPdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.CompressPdf.route) { 
                com.spinel.pdftools.ui.compresspdf.CompressPdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.OrganizePdf.route) { 
                OrganizePdfScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.PdfToJpg.route) {
                PdfToJpgScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.PdfViewer.route,
                arguments = listOf(navArgument("encodedUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriString = backStackEntry.arguments?.getString("encodedUri") ?: ""
                PdfViewerScreen(
                    uriString = uriString,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
