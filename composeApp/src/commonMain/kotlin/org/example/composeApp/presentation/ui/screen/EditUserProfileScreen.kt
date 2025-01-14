package org.example.composeApp.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.create_profile_button_label
import learnflex.composeapp.generated.resources.delete_profile_button_label
import learnflex.composeapp.generated.resources.sign_out_button_label
import org.example.composeApp.presentation.navigation.AppDestination
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.ui.component.CustomScaffold
import org.example.composeApp.presentation.ui.component.CustomVerticalScrollbar
import org.example.composeApp.presentation.ui.component.ProfileForm
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.viewModel.EditUserProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.example.composeApp.presentation.action.EditUserProfileAction as Action

/**
 * Composable function that displays the Edit User Profile screen.
 *
 * @param windowSizeClass The window size class that determines the layout of the screen.
 * @param navController The navigation controller used to navigate between composables.
 * @param viewModel The view model that handles the business logic of the Edit User Profile screen.
 */
@Composable
fun EditUserProfileScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: EditUserProfileViewModel
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle()
    )

    HandleUIEvents(Route.EditProfile, navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }

    val isLoading = screenConfig.uiState.value.isUploading && screenConfig.uiState.value.isDownloading
    val scrollState = rememberScrollState()

    CustomScaffold(
        snackbarHostState = screenConfig.snackbarHostState,
        snackbarType = screenConfig.snackbarType.value,
        currentDestination = AppDestination.ProfileManagement,
        onDestinationSelected = { navController.navigate(it.route) },
        onRefresh = { viewModel.handleAction(Action.Refresh) },
        enabled = !isLoading
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(400.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileForm(
                    isLoading = isLoading,
                    photoUrl = screenConfig.uiState.value.photoUrl,
                    username = screenConfig.uiState.value.username,
                    usernameError = screenConfig.uiState.value.usernameError,
                    goal = screenConfig.uiState.value.goal,
                    level = screenConfig.uiState.value.level,
                    isLevelDropdownVisible = screenConfig.uiState.value.isLevelDropdownVisible,
                    onImageSelected = { viewModel.handleAction(Action.UploadProfilePicture(it)) },
                    onImageDeleted = { viewModel.handleAction(Action.DeleteProfilePicture) },
                    currentImageUrl = screenConfig.uiState.value.photoDownloadUrl,
                    onHandleError = { viewModel.handleAction(Action.HandleError(it)) },
                    onUsernameChange = { viewModel.handleAction(Action.EditUsername(it)) },
                    onGoalChange = { viewModel.handleAction(Action.EditGoal(it)) },
                    onToggleLevelDropdownVisibility = { viewModel.handleAction(Action.ToggleLevelDropdownVisibility) },
                    onSelectLevel = { viewModel.handleAction(Action.SelectLevel(it)) },
                    onSelectField = { viewModel.handleAction(Action.SelectField(it)) },
                    onSubmit = { viewModel.handleAction(Action.EditProfile) },
                    submitText = stringResource(Res.string.create_profile_button_label),
                    modifier = Modifier.fillMaxSize().padding(Padding.MEDIUM.dp)
                )
                OutlinedButton(
                    onClick = { viewModel.handleAction(Action.SignOut) },
                    modifier = Modifier.fillMaxWidth()
                        .height(Dimension.AUTH_BUTTON_HEIGHT.dp),
                    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp)
                ) {
                    Text(stringResource(Res.string.sign_out_button_label))
                }
                Button(
                    onClick = { viewModel.handleAction(Action.DeleteProfile) },
                    modifier = Modifier.fillMaxWidth()
                        .height(Dimension.AUTH_BUTTON_HEIGHT.dp),
                    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp)
                ) {
                    Text(stringResource(Res.string.delete_profile_button_label))
                }
            }
            if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            CustomVerticalScrollbar(scrollState)
        }
    }
}