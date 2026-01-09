package app.olauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.olauncher.MainViewModel
import app.olauncher.data.Constants
import app.olauncher.ui.home.HomeScreen
import app.olauncher.ui.settings.SettingsScreen
import app.olauncher.ui.theme.TLauncherTheme

@Composable
fun TLauncherApp(
    viewModel: MainViewModel,
    onOpenAccessibility: () -> Unit,
    onOpenUsageAccess: () -> Unit,
    onOpenNotificationListener: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    TLauncherTheme {
        NavHost(navController = navController, startDestination = "home") {
            
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { 
                        navController.navigate("appList/${Constants.FLAG_LAUNCH_APP}")
                    },
                    onNavigateToDashboard = { navController.navigate("dashboard") },
                    onNavigateToProductivity = { navController.navigate("productivity") },
                    onNavigateToPhone = { app.olauncher.helper.openDialerApp(context) },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onAppClick = { index -> 
                         viewModel.selectedApp(
                             app.olauncher.data.AppModel(
                                 appLabel = viewModel.prefs.getAppName(index),
                                 appPackage = viewModel.prefs.getAppPackage(index),
                                 activityClassName = viewModel.prefs.getAppActivityClassName(index),
                                 user = app.olauncher.helper.getUserHandleFromString(context, viewModel.prefs.getAppUser(index)),
                                 // dummies
                                 isNew = false,
                                 isWhitelisted = false,
                                 key = null
                             ),
                             Constants.FLAG_LAUNCH_APP
                         )
                    },
                    onAppLongClick = { index ->
                         navController.navigate("appList/${Constants.FLAG_SET_HOME_APP_1 + index - 1}")
                    }
                )
            }
            
            composable("dashboard") {
                app.olauncher.ui.dashboard.DashboardScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("productivity") {
                app.olauncher.ui.productivity.ProductivityScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("focus") {
                app.olauncher.ui.focus.FocusModeScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(
                "appList/{flag}",
                arguments = listOf(androidx.navigation.navArgument("flag") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val flag = backStackEntry.arguments?.getInt("flag") ?: Constants.FLAG_LAUNCH_APP
                app.olauncher.ui.list.AppListScreen(
                    viewModel = viewModel,
                    flag = flag,
                    onAppClick = { appModel ->
                        viewModel.selectedApp(appModel, flag)
                        if (flag == Constants.FLAG_LAUNCH_APP) {
                            // Reset state/search if needed
                        } else {
                            // If selecting for home screen, pop back
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFocus = { navController.navigate("focus") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenAccessibility = onOpenAccessibility,
                    onOpenUsageAccess = onOpenUsageAccess,
                    onOpenNotificationListener = onOpenNotificationListener
                )
            }
        }
    }
}
