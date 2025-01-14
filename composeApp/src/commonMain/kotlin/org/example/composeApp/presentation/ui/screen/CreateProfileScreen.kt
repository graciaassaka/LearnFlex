package org.example.composeApp.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.CreateProfileUIState
import org.example.composeApp.presentation.state.CreateProfileUIState.ProfileCreationForm
import org.example.composeApp.presentation.ui.component.CustomVerticalScrollbar
import org.example.composeApp.presentation.ui.component.ProfileForm
import org.example.composeApp.presentation.ui.component.SelectableCardGroup
import org.example.composeApp.presentation.ui.component.create_profile.PersonalInfoForm
import org.example.composeApp.presentation.ui.component.create_profile.StyleQuestionnaireForm
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.layout.AlignedLabeledBarsLayout
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.viewModel.CreateUserProfileViewModel
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.profile.FetchStyleQuestionsUseCase
import org.jetbrains.compose.resources.stringResource
import org.example.composeApp.presentation.action.CreateUserProfileAction as Action


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

    HandleUIEvents(Route.CreateProfile, navController, viewModel, screenConfig.snackbarHostState) {
        screenConfig.snackbarType.value = it
    }

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

    val scrollState = rememberScrollState()

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
            ProfileForm(
                isLoading = uiState.value.isLoading,
                photoUrl = uiState.value.photoUrl,
                username = uiState.value.username,
                usernameError = uiState.value.usernameError,
                goal = uiState.value.goal,
                level = uiState.value.level,
                isLevelDropdownVisible = uiState.value.isLevelDropdownVisible,
                onImageSelected = { handleAction(Action.UploadProfilePicture(it)) },
                onImageDeleted = { handleAction(Action.DeleteProfilePicture) },
                onHandleError = { handleAction(Action.HandleError(it)) },
                onUsernameChange = { handleAction(Action.EditUsername(it)) },
                onGoalChange = { handleAction(Action.EditGoal(it)) },
                onToggleLevelDropdownVisibility = { handleAction(Action.ToggleLevelDropdownVisibility) },
                onSelectLevel = { handleAction(Action.SelectLevel(it)) },
                onSelectField = { handleAction(Action.SelectField(it)) },
                onSubmit = { handleAction(Action.CreateProfile) },
                submitText = stringResource(Res.string.create_profile_button_label),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Padding.MEDIUM.dp)
                    .verticalScroll(scrollState)
            )
            CustomVerticalScrollbar(scrollState)
        }
    }
}

@Composable
private fun StyleQuestionnaireScreen(
    screenConfig: ScreenConfig<CreateProfileUIState>,
    handleAction: (Action) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentQuestion = uiState.value.styleQuestionnaire.getOrNull(currentQuestionIndex)
    var selectedOption by rememberSaveable(currentQuestion) { mutableStateOf("") }
    val styleSaver = Saver<Style?, String>(
        save = { it?.name },
        restore = { savedValue -> savedValue.let(Style::valueOf) }
    )
    var selectedStyle by rememberSaveable(currentQuestion, stateSaver = styleSaver) { mutableStateOf(null) }

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
                isQuizFinished = currentQuestionIndex == FetchStyleQuestionsUseCase.NUMBER_OF_QUESTIONS - 1,
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
            onConfirm = { handleAction(Action.SetLearningStyle) },
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
        CustomVerticalScrollbar(scrollState)
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