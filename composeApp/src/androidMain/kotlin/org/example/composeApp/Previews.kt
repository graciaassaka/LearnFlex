package org.example.composeApp

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.CustomScaffold
import org.example.composeApp.navigation.AppDestination
import org.example.composeApp.screen.DashboardScreen
import org.example.composeApp.theme.LearnFlexTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=1920dp,height=1080dp,dpi=160"
)
@Composable
private fun DesktopDefaultPreview() {
    LearnFlexTheme {
        CustomScaffold(
            currentDestination = AppDestination.Dashboard,
            onDestinationSelected = {},
            enabled = true
        ) {
            DashboardScreen(WindowSizeClass.calculateFromSize(DpSize(1920.dp, 1080.dp)))
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "id:pixel_tablet"
)
@Composable
private fun TabletDefaultPreview() {
    LearnFlexTheme {
        CustomScaffold(
            currentDestination = AppDestination.Dashboard,
            onDestinationSelected = {},
            enabled = true
        ) {
            DashboardScreen(WindowSizeClass.calculateFromSize(DpSize(800.dp, 1280.dp)))
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun PhoneDefaultPreview() {
    LearnFlexTheme {
        CustomScaffold(
            currentDestination = AppDestination.Dashboard,
            onDestinationSelected = {},
            enabled = true
        ) {
            DashboardScreen(WindowSizeClass.calculateFromSize(DpSize(400.dp, 800.dp)))
        }
    }
}

