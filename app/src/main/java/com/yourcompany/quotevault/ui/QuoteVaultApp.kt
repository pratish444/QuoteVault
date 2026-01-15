package com.yourcompany.quotevault.ui


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourcompany.quotevault.ui.navigation.Screen
import com.yourcompany.quotevault.ui.screens.auth.*
import com.yourcompany.quotevault.ui.screens.collections.*
import com.yourcompany.quotevault.ui.screens.favorites.FavoritesScreen
import com.yourcompany.quotevault.ui.screens.home.HomeScreen
import com.yourcompany.quotevault.ui.screens.profile.EditProfileScreen
import com.yourcompany.quotevault.ui.screens.profile.ProfileScreen
import com.yourcompany.quotevault.ui.screens.search.SearchScreen
import com.yourcompany.quotevault.ui.screens.settings.SettingsScreen
import com.yourcompany.quotevault.ui.theme.QuoteVaultTheme

@Composable
fun QuoteVaultApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val userPreferences by authViewModel.userPreferences.collectAsStateWithLifecycle()

    QuoteVaultTheme(userPreferences = userPreferences) {
        val navController = rememberNavController()

        LaunchedEffect(authState.isAuthenticated) {
            if (!authState.isAuthenticated) {
                navController.navigate(Screen.Welcome.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        if (authState.isAuthenticated) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination

                        Screen.bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                        )
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable(Screen.Favorites.route) {
                        FavoritesScreen()
                    }
                    composable(Screen.Collections.route) {
                        CollectionsScreen(
                            onCollectionClick = { collectionId ->
                                navController.navigate(Screen.CollectionDetail.createRoute(collectionId))
                            }
                        )
                    }
                    composable(Screen.CollectionDetail.route) { backStackEntry ->
                        val collectionId = backStackEntry.arguments?.getString("collectionId") ?: return@composable
                        CollectionDetailScreen(
                            collectionId = collectionId,
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable(Screen.Profile.route) {
                        ProfileScreen(
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                            onSignOut = {
                                authViewModel.signOut()
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Settings.route) {
                        SettingsScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable(Screen.EditProfile.route) {
                        EditProfileScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                }
            }
        } else {
            // Auth flow
            NavHost(
                navController = navController,
                startDestination = Screen.Welcome.route
            ) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                    )
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onSignUpSuccess = { navController.navigate(Screen.Home.route) }
                    )
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                        onLoginSuccess = { navController.navigate(Screen.Home.route) }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onBackClick = { navController.navigateUp() }
                    )
                }
            }
        }
    }
}