package org.example.composeApp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * AppDestination enum class.
 *
 * @param label The label of the destination.
 * @param route The route of the destination.
 * @param icon The icon of the destination.
 * @param contentDescription The content description of the destination.
 */
enum class AppDestination(
    val label: String,
    val route: Route,
    val icon: ImageVector,
    val contentDescription: String
) {
    Dashboard(
        label = "Dashboard",
        route = Route.Dashboard,
        icon = Icons.Default.Dashboard,
        contentDescription = "Dashboard"
    ),
    Library(
        label = "Library",
        route = Route.Library,
        icon = Icons.AutoMirrored.Filled.LibraryBooks,
        contentDescription = "Library"
    ),
    Study(
        label = "Study",
        route = Route.Study(),
        icon = Icons.Default.LocalLibrary,
        contentDescription = "Study"
    ),
    ProfileManagement(
        label = "Profile",
        route = Route.ProfileManagement,
        icon = Icons.Default.Person,
        contentDescription = "Profile"
    )
}