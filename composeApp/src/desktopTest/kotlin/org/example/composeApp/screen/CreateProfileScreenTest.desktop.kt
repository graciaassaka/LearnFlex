package org.example.composeApp.screen

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import org.example.composeApp.theme.LearnFlexTheme
import org.example.composeApp.util.LocalComposition
import org.example.composeApp.util.TestTags
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.*
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.ProfileCreationForm
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateProfileScreenTest {
    private lateinit var navController: NavController
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var uiState: MutableStateFlow<CreateProfileUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @BeforeTest
    fun setUp() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(CreateProfileUIState())
        uiEventFlow = MutableSharedFlow()
        windowSizeClass = WindowSizeClass.calculateFromSize(
            DpSize(width = 800.dp, height = 800.dp),
            setOf(WindowWidthSizeClass.Expanded),
            setOf(WindowHeightSizeClass.Expanded)
        )

        every { viewModel.state } returns uiState
        every { viewModel.uiEvent } returns uiEventFlow as SharedFlow<UIEvent>
        every { viewModel.isScreenVisible } returns MutableStateFlow(true)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun createProfileScreenDisplaysCorrectly() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        onNodeWithTag(TestTags.PERSONAL_INFO_IMAGE_UPLOAD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.PERSONAL_INFO_FIELD_PICKER.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun usernameTextField_calls_viewModel_onUsernameChanged_whenTextEntered() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        val username = "TestUser"
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with) {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update {
                        it.copy(
                            username = username,
                            usernameError = message
                        )
                    }
                }
            }
        }

        onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        onNodeWithText(username).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun usernameTextField_displaysError_whenInvalidUsernameEntered() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        val username = "Test User"
        val errorMessage = (InputValidator.validateUsername(username) as ValidationResult.Invalid).message
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with) {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update {
                        it.copy(
                            username = username,
                            usernameError = message
                        )
                    }
                }
            }
        }

        onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun goalTextField_calls_viewModel_onGoalChanged_whenTextEntered() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        val goal = "TestGoal"
        every { viewModel.onGoalChanged(goal) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).performTextInput(goal)

        verify { viewModel.onGoalChanged(goal) }
        onNodeWithText(goal).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun levelDropdown_calls_viewModel_onLevelChanged_whenSelected() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        val level = Level.Advanced
        every {
            viewModel.toggleLevelDropdownVisibility()
        } answers {
            uiState.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }
        }
        every { viewModel.onLevelChanged(level) } answers {
            uiState.update { it.copy(level = level) }
        }

        onNodeWithTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN_BUTTON.tag).performClick()
        verify { viewModel.toggleLevelDropdownVisibility() }

        onNodeWithText(Level.Advanced.name).performClick()
        verify { viewModel.onLevelChanged(level) }

        onNodeWithText("Advanced").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun fieldPicker_calls_viewModel_onFieldChanged_whenScrolled() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        onNodeWithTag("${Field::class.simpleName}_picker")
            .performTouchInput {
                down(Offset(10f, 0f))
                moveTo(Offset(10f, -100f))
                up()
            }

        verify { viewModel.onFieldChanged(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun createProfileButton_calls_viewModel_onCreateProfile_whenClicked() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }
        onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).performClick()

        verify { viewModel.onCreateProfile(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun personalInfoForm_disables_buttons_and_fields_when_loading() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update { it.copy(isLoading = true) }

        onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).assertIsNotEnabled()
        onNodeWithTag("${Level::class.simpleName}_dropdown_button").assertIsNotEnabled()
        onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireForm_displays_correctly() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE.tag).assertIsEnabled()
        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_OPTIONS_GROUP.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireOptionsGroup_selects_option_whenClicked() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        onNodeWithText(Style.READING.value).performClick()
        onNodeWithText(Style.READING.value).assertIsSelected()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireOptionsGroup_calls_viewModel_onQuestionAnswered_whenNextButtonClicked() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        onNodeWithText(Style.READING.value).performClick()
        onNodeWithText(Style.READING.value).assertIsSelected()

        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag).performClick()
        verify { viewModel.onQuestionAnswered(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireResultDialog_displays_correctly() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_RESULT_DIALOG.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_BREAKDOWN.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireResultDialog_calls_viewModel_setLearningStyle_whenButtonClicked() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag).performClick()
        verify { viewModel.setLearningStyle(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun styleQuestionnaireResultDialog_calls_viewModel_startStyleQuestionnaire_whenButtonClicked() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
                LearnFlexTheme {
                    CreateProfileScreen(windowSizeClass, navController, viewModel)
                }
            }
        }

        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_RESTART_BUTTON.tag).performClick()
        verify { viewModel.startStyleQuestionnaire() }
    }

    companion object {
        private val styleQuestionnaire = listOf(
                StyleQuestion(
                    listOf(StyleOption(Style.READING.value, Style.READING.value)), "Question 1",
                )
            )

        private val learningStyle = LearningStyle(
            dominant = Style.READING.value,
            breakdown = StyleBreakdown(visual = 0, reading = 50, kinesthetic = 50)
        )
    }
}