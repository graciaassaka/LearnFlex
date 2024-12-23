package org.example.composeApp.screen

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
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.*
import org.example.composeApp.component.create_profile.PersonalInfoForm
import org.example.composeApp.component.create_profile.StyleQuestionnaireForm
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.layout.AlignedLabeledBarsLayout
import org.example.composeApp.layout.EnumScrollablePickerLayout
import org.example.composeApp.util.TestTags
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Field
import org.example.shared.domain.model.LearningStyleBreakdown
import org.example.shared.domain.model.Level
import org.example.shared.domain.model.StyleQuestion
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.ProfileCreationForm
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.jetbrains.compose.resources.stringResource

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
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf<SnackbarType>(SnackbarType.Info) }

    val uiState by viewModel.state.collectAsState()
    val isScreenVisible by viewModel.isScreenVisible.collectAsState()

    HandleUIEvents(Route.CreateProfile, navController, viewModel, snackbarHostState) { currentSnackbarType = it }

    when (uiState.currentForm) {
        ProfileCreationForm.PERSONAL_INFO -> {
            PersonalInfoScreen(
                windowSizeClass = windowSizeClass,
                snackbarHostState = snackbarHostState,
                currentSnackbarType = currentSnackbarType,
                enabled = !uiState.isLoading,
                displayForm = viewModel::displayProfileCreationForm,
                onUploadProfilePicture = { data, msg -> viewModel.onUploadProfilePicture(data, msg) },
                onProfilePictureDeleted = viewModel::onProfilePictureDeleted,
                handleError = viewModel::handleError,
                photoUrl = uiState.photoUrl,
                username = uiState.username,
                onUsernameChanged = viewModel::onUsernameChanged,
                usernameError = uiState.usernameError,
                goal = uiState.goal,
                onGoalChanged = viewModel::onGoalChanged,
                level = uiState.level,
                onLevelChanged = viewModel::onLevelChanged,
                isLevelDropdownVisible = uiState.isLevelDropdownVisible,
                toggleLevelDropdownVisibility = viewModel::toggleLevelDropdownVisibility,
                onFieldChanged = viewModel::onFieldChanged,
                onCreateProfile = viewModel::onCreateProfile,
                isProfileCreated = uiState.isProfileCreated,
                startStyleQuestionnaire = viewModel::startStyleQuestionnaire,
                modifier = Modifier.testTag(TestTags.PERSONAL_INFO.tag)
            )
        }

        ProfileCreationForm.STYLE_QUESTIONNAIRE -> {
            StyleQuestionnaireScreen(
                windowSizeClass = windowSizeClass,
                snackbarHostState = snackbarHostState,
                currentSnackbarType = currentSnackbarType,
                enabled = !uiState.isLoading,
                isScreenVisible = isScreenVisible,
                onExitAnimationFinished = viewModel::onExitAnimationFinished,
                styleQuestionnaire = uiState.styleQuestionnaire,
                questionCount = viewModel.questionCount,
                onQuestionAnswered = viewModel::onQuestionAnswered,
                onQuestionnaireCompleted = viewModel::onQuestionnaireCompleted,
                startStyleQuestionnaire = viewModel::startStyleQuestionnaire,
                setLearningStyle = viewModel::setLearningStyle,
                handleError = viewModel::handleError,
                showStyleBreakdownDialog = uiState.showStyleResultDialog,
                learningStyleBreakdown = uiState.learningStyle?.breakdown,
                modifier = Modifier.testTag(TestTags.STYLE_QUESTIONNAIRE.tag)
            )
        }
    }
}

@Composable
private fun PersonalInfoScreen(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    currentSnackbarType: SnackbarType,
    enabled: Boolean,
    displayForm: (ProfileCreationForm) -> Unit,
    onUploadProfilePicture: (ByteArray, String) -> Unit,
    onProfilePictureDeleted: (String) -> Unit,
    handleError: (Throwable) -> Unit,
    photoUrl: String,
    username: String,
    onUsernameChanged: (String) -> Unit,
    usernameError: String?,
    goal: String,
    onGoalChanged: (String) -> Unit,
    level: Level,
    onLevelChanged: (Level) -> Unit,
    isLevelDropdownVisible: Boolean,
    toggleLevelDropdownVisibility: () -> Unit,
    onFieldChanged: (Field) -> Unit,
    onCreateProfile: (String) -> Unit,
    isProfileCreated: Boolean,
    startStyleQuestionnaire: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFormVisible by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf<ProfileCreationForm?>(null) }
    var goalCharCount by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    val maxGoalLen = 80
    val createProfileSuccessMsg = stringResource(Res.string.create_profile_success)
    val uploadPhotoSuccessMsg = stringResource(Res.string.update_photo_success)
    val deletePhotoSuccessMsg = stringResource(Res.string.delete_photo_success)

    LaunchedEffect(isProfileCreated) {
        if (isProfileCreated) {
            startStyleQuestionnaire()
            isFormVisible = false
            currentDestination = ProfileCreationForm.STYLE_QUESTIONNAIRE
        }
    }

    PersonalInfoForm(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = currentSnackbarType,
        caption = stringResource(Res.string.create_profile_screen_title),
        enabled = enabled,
        isVisible = isFormVisible,
        onAnimationFinished = { currentDestination?.let { displayForm(it) } },
        modifier = modifier
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            CustomVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
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
                    enabled = enabled,
                    onImageSelected = { onUploadProfilePicture(it, uploadPhotoSuccessMsg) },
                    onImageDeleted = { onProfilePictureDeleted(deletePhotoSuccessMsg) },
                    handleError = handleError,
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_IMAGE_UPLOAD.tag),
                    isUploaded = photoUrl.isBlank().not()
                )
                TextField(
                    value = username,
                    onValueChange = onUsernameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag),
                    enabled = enabled,
                    label = { Text(stringResource(Res.string.username_label)) },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, null) },
                    supportingText = { usernameError?.let { Text(it) } },
                    isError = usernameError != null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                TextField(
                    value = goal,
                    onValueChange = {
                        if (it.length < maxGoalLen) onGoalChanged(it)
                        goalCharCount = it.length
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag),
                    enabled = enabled,
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
                    selected = level,
                    isDropDownVisible = isLevelDropdownVisible,
                    onDropDownVisibilityChanged = toggleLevelDropdownVisibility,
                    onSelected = onLevelChanged,
                    enabled = enabled,
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN.tag)
                )
                EnumScrollablePickerLayout<Field>(
                    label = stringResource(Res.string.field_label),
                    onChange = onFieldChanged,
                    enabled = enabled,
                    modifier = Modifier.testTag(TestTags.PERSONAL_INFO_FIELD_PICKER.tag)
                )
                Button(
                    onClick = { onCreateProfile(createProfileSuccessMsg) },
                    enabled = enabled && usernameError.isNullOrBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                        .testTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag),
                    shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                    content = { Text(stringResource(Res.string.create_profile_button_label)) }
                )
                Spacer(modifier = Modifier.height(Spacing.LARGE.dp))
            }
        }
    }
}

@Composable
private fun StyleQuestionnaireScreen(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    currentSnackbarType: SnackbarType,
    enabled: Boolean,
    isScreenVisible: Boolean,
    onExitAnimationFinished: () -> Unit,
    styleQuestionnaire: List<StyleQuestion>,
    questionCount: Int,
    onQuestionAnswered: (Style) -> Unit,
    onQuestionnaireCompleted: () -> Unit,
    startStyleQuestionnaire: () -> Unit,
    setLearningStyle: (String) -> Unit,
    handleError: (Throwable) -> Unit,
    showStyleBreakdownDialog: Boolean,
    learningStyleBreakdown: LearningStyleBreakdown?,
    modifier: Modifier = Modifier
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val currentQuestion = styleQuestionnaire.getOrNull(currentQuestionIndex)
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
        snackbarType = currentSnackbarType,
        question = currentQuestion?.scenario ?: "",
        enabled = enabled && currentQuestionIndex < styleQuestionnaire.size,
        isVisible = isScreenVisible,
        onAnimationFinished = onExitAnimationFinished,
        modifier = modifier
    ) {
        currentQuestion?.let { question ->
            QuestionContent(
                question = question,
                selectedOption = selectedOption,
                enabled = enabled && currentQuestionIndex < styleQuestionnaire.size,
                onOptionSelected = { option ->
                    selectedOption = option
                    selectedStyle = try {
                        Style.valueOf(question.options.first { it.text == option }.style.uppercase())
                    } catch (e: Exception) {
                        handleError(e)
                        null
                    }
                },
                isQuizFinished = currentQuestionIndex == questionCount - 1,
                onNextClicked = {
                    selectedStyle?.let { style ->
                        onQuestionAnswered(style)
                        if (currentQuestionIndex < styleQuestionnaire.size) currentQuestionIndex++
                    }
                },
                onFinish = onQuestionnaireCompleted
            )
        }
    }

    if (showStyleBreakdownDialog && learningStyleBreakdown != null) {
        StyleBreakdownDialog(
            enabled = enabled,
            learningStyleBreakdown = learningStyleBreakdown,
            onConfirm = { setLearningStyle(successMessage) },
            onDismiss = {
                currentQuestionIndex = 0
                startStyleQuestionnaire()
            }
        )
    }
}

@Composable
private fun QuestionContent(
    question: StyleQuestion,
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
        CustomVerticalScrollbar(
            scrollState = scrollState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
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
    }
}

@Composable
private fun StyleBreakdownDialog(
    enabled: Boolean,
    learningStyleBreakdown: LearningStyleBreakdown,
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
                    learningStyleBreakdown.visual / 100f,
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
