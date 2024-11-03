package org.example.composeApp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.EnumDropdown
import org.example.composeApp.component.EnumScrollablePicker
import org.example.composeApp.component.HandleUIEvents
import org.example.composeApp.component.ImageUpload
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.layout.CreateProfileLayout
import org.example.shared.data.model.Field
import org.example.shared.data.model.Level
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.jetbrains.compose.resources.stringResource

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
    viewModel: CreateUserProfileViewModel
)
{
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf<SnackbarType>(SnackbarType.Info) }
    var goalCharCount by remember { mutableIntStateOf(0) }

    val uiState by viewModel.state.collectAsState()
    val isScreenVisible by viewModel.isScreenVisible.collectAsState()

    val createProfileSuccessMsg = stringResource(Res.string.create_profile_success)
    val uploadPhotoSuccessMsg = stringResource(Res.string.update_photo_success)
    val deletePhotoSuccessMsg = stringResource(Res.string.delete_photo_success)

    HandleUIEvents(Route.CreateProfile, navController, viewModel, snackbarHostState) { currentSnackbarType = it }

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
                .padding(vertical = Padding.LARGE.dp, horizontal = Padding.MEDIUM.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageUpload(
                isLoading = uiState.isLoading,
                onImageSelected = {viewModel.onUploadProfilePicture(it, uploadPhotoSuccessMsg)},
                onImageDeleted = { viewModel.onProfilePictureDeleted(deletePhotoSuccessMsg) },
                handleError = { viewModel.showSnackbar(it, SnackbarType.Error) },
                modifier = Modifier.testTag("imageUpload"),
                isUploaded = uiState.photoUrl.isBlank().not()
            )
            TextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("usernameTextField"),
                enabled = !uiState.isLoading,
                label = { Text(stringResource(Res.string.username_label)) },
                leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                supportingText = { uiState.usernameError?.let { Text(it) } },
                isError = uiState.usernameError != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            TextField(
                value = uiState.goal,
                onValueChange = {
                    if (it.length < 80) viewModel.onGoalChanged(it)
                    goalCharCount = it.length
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goalTextField"),
                enabled = !uiState.isLoading,
                label = { Text(stringResource(Res.string.goals_label)) },
                leadingIcon = { Icon(Icons.Default.SportsFootball, null) },
                supportingText = { Text("$goalCharCount/80") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = false
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
            EnumScrollablePicker<Field>(
                label = stringResource(Res.string.field_label),
                onChange = viewModel::onFieldChanged,
                enabled = !uiState.isLoading,
                modifier = Modifier.testTag("fieldPicker")
            )
            Button(
                onClick = { viewModel.onCreateProfile(createProfileSuccessMsg) },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                    .testTag("createProfileButton"),
                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                content = { Text(stringResource(Res.string.create_profile_button_label)) }
            )
        }
    }
}