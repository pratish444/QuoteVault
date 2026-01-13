package com.yourcompany.quotevault.ui.navigation


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Welcome : Screen("welcome", "Welcome")
    object SignUp : Screen("signup", "Sign Up")
    object Login : Screen("login", "Login")
    object ForgotPassword : Screen("forgot_password", "Forgot Password")

    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object Collections : Screen("collections", "Collections", Icons.Default.FolderOpen)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Settings : Screen("settings", "Settings")

    object CollectionDetail : Screen("collection/{collectionId}", "Collection") {
        fun createRoute(collectionId: String) = "collection/$collectionId"
    }

    companion object {
        val bottomNavItems = listOf(Home, Search, Favorites, Collections, Profile)
    }
}