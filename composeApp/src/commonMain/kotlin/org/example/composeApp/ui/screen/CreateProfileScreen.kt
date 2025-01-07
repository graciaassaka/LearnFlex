package org.example.composeApp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.ui.component.*
import org.example.composeApp.ui.component.create_profile.PersonalInfoForm
import org.example.composeApp.ui.component.create_profile.StyleQuestionnaireForm
import org.example.composeApp.ui.dimension.Dimension
import org.example.composeApp.ui.dimension.Padding
import org.example.composeApp.ui.dimension.Spacing
import org.example.composeApp.ui.layout.AlignedLabeledBarsLayout
import org.example.composeApp.ui.layout.EnumScrollablePickerLayout
import org.example.composeApp.ui.util.ScreenConfig
import org.example.composeApp.ui.util.TestTags
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.profile.FetchStyleQuestionnaireUseCase
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.ProfileCreationForm
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.jetbrains.compose.resources.stringResource
import org.example.shared.presentation.action.CreateUserProfileAction as Action

/**
 * A composable function that displays the CreateProfileScreen with a form for user input.
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
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle()
    )

    HandleUIEvents(Route.CreateProfile, navController, viewModel, screenConfig.snackbarHostState) { screenConfig.snackbarType.value = it }

    when (screenConfig.uiState.value.currentForm) {
        ProfileCreationForm.PersonalInfo -> {
            PersonalInfoScreen(
                screenConfig = screenConfig,
                handleAction = viewModel::handleAction,
                modifier = Modifier.testTag(TestTags.PERSONAL_INFO.tag)
            )
        }

        ProfileCreationForm.StyleQuestionnaire -> {
            StyleQuestionnaireScreen(
                screenConfig = screenConfig,
                handleAction = viewModel::handleAction,
                modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE.tag)
            )
        }
    }
}

@Composable
private fun PersonalInfoScreen(
    screenConfig: ScreenConfig<CreateProfileUIState>,
    handleAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var isFormVisible by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf<ProfileCreationForm?>(null) }
    var goalCharCount by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val maxGoalLen = 80
    val createProfileSuccessMsg = stringResource(Res.string.create_profile_success)
    val uploadPhotoSuccessMsg = stringResource(Res.string.update_photo_success)
    val deletePhotoSuccessMsg = stringResource(Res.string.delete_photo_success)

    LaunchedEffect(uiState.value.isProfileCreated) {
        if (uiState.value.isProfileCreated) {
            handleAction(Action.StartStyleQuestionnaire)
            isFormVisible = false
            currentDestination = ProfileCreationForm.StyleQuestionnaire
        }
    }

    PersonalInfoForm(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        caption = stringResource(Res.string.create_profile_screen_title),
        enabled = !uiState.value.isLoading,
        isVisible = isFormVisible,
        onAnimationFinished = {
            currentDestination?.let {
                handleAction(Action.DisplayProfileCreationForm(it))
            }
        },
        modifier = modifier
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Padding.MEDIUM.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Padding.MEDIUM.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
                ImageUpload(
                    enabled = !uiState.value.isLoading,
                    onImageSelected = { imageData -> handleAction(Action.UploadProfilePicture(imageData, uploadPhotoSuccessMsg)) },
                    onImageDeleted = { handleAction(Action.DeleteProfilePicture(deletePhotoSuccessMsg)) },
                    handleError = { error: Throwable -> handleAction(Action.HandleError(error)) },
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_IMAGE_UPLOAD.tag),
                    isUploaded = uiState.value.photoUrl.isBlank().not()
                )
                TextField(
                    value = uiState.value.username,
                    onValueChange = { handleAction(Action.EditUsername(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag),
                    enabled = !uiState.value.isLoading,
                    label = { Text(stringResource(Res.string.username_label)) },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                    supportingText = { Text(uiState.value.usernameError ?: "") },
                    isError = uiState.value.usernameError != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                TextField(
                    value = uiState.value.goal,
                    onValueChange = {
                        if (it.length < maxGoalLen) handleAction(Action.EditGoal(it))
                        goalCharCount = it.length
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag),
                    enabled = !uiState.value.isLoading,
                    label = { Text(stringResource(Res.string.goals_label)) },
                    leadingIcon = { Icon(Icons.Default.SportsFootball, null) },
                    supportingText = {
                        Text(
                            text = "$goalCharCount/$maxGoalLen",
                            modifier = Modifier.testTag(TestTags.PERSONAL_INFO_GOAL_CHAR_COUNTER.tag)
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    singleLine = false
                )
                EnumDropdown<Level>(
                    label = stringResource(Res.string.level_label),
                    selected = uiState.value.level,
                    isDropDownVisible = uiState.value.isLevelDropdownVisible,
                    onDropDownVisibilityChanged = { handleAction(Action.ToggleLevelDropdownVisibility) },
                    onSelected = { handleAction(Action.SelectLevel(it)) },
                    enabled = !uiState.value.isLoading,
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN.tag)
                )
                EnumScrollablePickerLayout<Field>(
                    label = stringResource(Res.string.field_label),
                    onChange = { handleAction(Action.SelectField(it)) },
                    enabled = !uiState.value.isLoading,
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_FIELD_PICKER.tag)
                )
                Button(
                    onClick = { handleAction(Action.CreateProfile(createProfileSuccessMsg)) },
                    enabled = !uiState.value.isLoading && uiState.value.usernameError.isNullOrBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                        .testTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag),
                    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                    content = { Text(stringResource(Res.string.create_profile_button_label)) }
                )
                Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
            }
            CustomVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
private fun StyleQuestionnaireScreen(
    screenConfig: ScreenConfig<CreateProfileUIState>,
    handleAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val currentQuestion = uiState.value.styleQuestionnaire.getOrNull(currentQuestionIndex)
    var selectedOption by rememberSaveable(currentQuestion) { mutableStateOf("") }
    val styleSaver = Saver<Style?, String>(
        save = { it?.value },
        restore = { savedValue -> savedValue.let { Style.valueOf(it.uppercase()) } }
    )
    var selectedStyle by rememberSaveable(currentQuestion, stateSaver = styleSaver) { mutableStateOf(null) }

    val successMessage = stringResource(Res.string.set_learning_style_success)

    StyleQuestionnaireForm(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        question = currentQuestion?.scenario ?: "",
        enabled = !uiState.value.isLoading && currentQuestionIndex < uiState.value.styleQuestionnaire.size,
        isVisible = isScreenVisible.value,
        onAnimationFinished = { handleAction(Action.HandleAnimationEnd) },
        modifier = modifier
    ) {
        currentQuestion?.let { question ->
            QuestionContent(
                question = question,
                selectedOption = selectedOption,
                enabled = !uiState.value.isLoading && currentQuestionIndex < uiState.value.styleQuestionnaire.size,
                onOptionSelected = { option ->
                    selectedOption = option
                    selectedStyle = try {
                        Style.valueOf(question.options.first { it.text == option }.style.uppercase())
                    } catch (e: Exception) {
                        handleAction(Action.HandleError(e))
                        null
                    }
                },
                isQuizFinished = currentQuestionIndex == FetchStyleQuestionnaireUseCase.NUMBER_OF_QUESTIONS - 1,
                onNextClicked = {
                    selectedStyle?.let { style ->
                        handleAction(Action.HandleQuestionAnswered(style))
                        if (currentQuestionIndex < uiState.value.styleQuestionnaire.size) currentQuestionIndex++
                    }
                },
                onFinish = { handleAction(Action.HandleQuestionnaireCompleted) },
            )
        }
    }

    if (uiState.value.showStyleResultDialog && uiState.value.learningStyle?.breakdown != null) {
        StyleBreakdownDialog(
            enabled = !uiState.value.isLoading,
            learningStyleBreakdown = uiState.value.learningStyle!!.breakdown,
            onConfirm = { handleAction(Action.SetLearningStyle(successMessage)) },
            onDismiss = {
                currentQuestionIndex = 0
                handleAction(Action.StartStyleQuestionnaire)
            }
        )
    }
}

@Composable
private fun QuestionContent(
    question: StyleQuizGeneratorClient.StyleQuestion,
    selectedOption: String,
    enabled: Boolean,
    onOptionSelected: (String) -> Unit,
    isQuizFinished: Boolean,
    onNextClicked: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SelectableCardGroup(
                options = question.options.map { it.text },
                onOptionSelected = onOptionSelected,
                selectedOption = selectedOption,
                enabled = enabled,
                modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE_OPTIONS_GROUP.tag)
            )
            Button(
                onClick = if (isQuizFinished) onFinish else onNextClicked,
                enabled = enabled && selectedOption.isNotBlank(),
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag),
            ) {
                Text(
                    if (isQuizFinished) stringResource(Res.string.finish_button_label)
                    else stringResource(Res.string.next_button_label)
                )
            }
        }
        CustomVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun StyleBreakdownDialog(
    enabled: Boolean,
    learningStyleBreakdown: Profile.LearningStyleBreakdown,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) = AlertDialog(
    onDismissRequest = {},
    confirmButton = {
        TextButton(
            onClick = onConfirm,
            enabled = enabled,
            modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag),
        ) {
            Text(stringResource(Res.string.finish_button_label))
        }
    },
    modifier = modifier.testTag(TestTags.STYLE_QUESTIONNAIRE_RESULT_DIALOG.tag),
    dismissButton = {
        TextButton(
            onClick = onDismiss,
            enabled = enabled,
            modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE_RESTART_BUTTON.tag),
        ) {
            Text(stringResource(Res.string.try_again_button_label))
        }
    },
    icon = { Icon(Icons.Default.Check, null) },
    title = { Text(stringResource(Res.string.style_result_dialog_title)) },
    text = {
        Column(
            modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE_BREAKDOWN.tag),
            verticalArrangement = Arrangement.spacedBy(Padding.SMALL.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val labels = remember { Style.entries.map { it.value } }
            val bars = remember(learningStyleBreakdown) {
                listOf(
                    learningStyleBreakdown.reading / 100f,
                    learningStyleBreakdown.kinesthetic / 100f
                )
            }
            AlignedLabeledBarsLayout(
                labels = labels,
                bars = bars,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
)