with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports for AboutScreen and PrivacyPolicyScreen
imports = """import com.spinel.pdftools.ui.about.AboutScreen
import com.spinel.pdftools.ui.about.PrivacyPolicyScreen
"""
content = content.replace('import com.spinel.pdftools.ui.settings.SettingsScreen\n', 'import com.spinel.pdftools.ui.settings.SettingsScreen\n' + imports)

# We want to show bottom bar conditionally. 
# find current implementation
bottom_bar_start = content.find('bottomBar = {')
bottom_bar_end = content.find('}    ) { innerPadding ->')

bottom_bar_replacement = """bottomBar = {
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
        """

content = content[:bottom_bar_start] + bottom_bar_replacement + content[bottom_bar_end:]

# update Settings composable call and add Privacy / About
routes_start = content.find('composable(Screen.Settings.route) { SettingsScreen() }')

routes_replacement = """composable(Screen.Settings.route) { 
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
            }"""

content = content.replace('composable(Screen.Settings.route) { SettingsScreen() }', routes_replacement)

with open('app/src/main/java/com/spinel/pdftools/ui/navigation/NavGraph.kt', 'w', encoding='utf-8') as f:
    f.write(content)
