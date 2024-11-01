package org.example.composeApp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.AnimatedOutlinedTextField
import org.example.composeApp.component.EnumDropdown
import org.example.composeApp.component.EnumScrollablePicker
import org.example.composeApp.component.HandleUIEvents
import org.example.composeApp.dimension.Padding
import org.example.composeApp.layout.CreateProfileLayout
import org.example.shared.data.model.Field
import org.example.shared.data.model.Level
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A composable function that displays the create profile screen with a form for user input.
 *
 * @param windowSizeClass The dimension classes for width and height of the window.
 * @param navController The navigation controller that manages the navigation within the application.
 * @param viewModel The view model that provides the data for the screen.
 */
@Composable
fun CreateProfileScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: CreateUserProfileViewModel = koinViewModel()
)
{
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf<SnackbarType>(SnackbarType.Info) }
    HandleUIEvents(Route.CreateProfile, navController, viewModel, snackbarHostState) { currentSnackbarType = it }
    val uiState by viewModel.state.collectAsState()
    val isScreenVisible by viewModel.isScreenVisible.collectAsState()
    val successMessage = stringResource(Res.string.create_profile_success)

    CreateProfileLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = currentSnackbarType,
        title = stringResource(Res.string.create_profile_screen_title),
        isLoading = uiState.isLoading,
        isVisible = isScreenVisible,
        onAnimationFinished = viewModel::onExitAnimationFinished,
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Padding.LARGE.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedOutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                enabled = !uiState.isLoading,
                modifier = Modifier.testTag("usernameTextField"),
                label = { Text(stringResource(Res.string.username_label)) },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                supportingText = { uiState.usernameError?.let { Text(it) } },
                isError = uiState.usernameError != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            EnumScrollablePicker<Field>(
                label = stringResource(Res.string.field_label),
                onChange = viewModel::onFieldChanged,
                enabled = !uiState.isLoading,
                modifier = Modifier.testTag("fieldPicker")
            )
            EnumDropdown<Level>(
                label = stringResource(Res.string.level_label),
                selected = uiState.level,
                isDropDownVisible = uiState.isLevelDropdownVisible,
                onDropDownVisibilityChanged = viewModel::toggleLevelDropdownVisibility,
                onSelected = viewModel::onLevelChanged,
                enabled = !uiState.isLoading,
                modifier = Modifier.testTag("levelDropdown")
            )
            Button(
                onClick = { viewModel.onCreateProfile(successMessage) },
                enabled = !uiState.isLoading,
                modifier = Modifier.testTag("submitButton"),
                content = { Text(stringResource(Res.string.create_profile_button_label)) }
            )
        }
    }
}