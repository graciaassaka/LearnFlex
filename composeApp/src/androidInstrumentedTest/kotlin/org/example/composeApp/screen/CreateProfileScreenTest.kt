package org.example.composeApp.screen

import android.content.Context
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import org.example.composeApp.presentation.state.CreateProfileUIState
import org.example.composeApp.presentation.state.CreateProfileUIState.ProfileCreationForm
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.screen.CreateProfileScreen
import org.example.composeApp.presentation.ui.theme.LearnFlexTheme
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.CreateUserProfileViewModel
import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.example.composeApp.presentation.action.CreateUserProfileAction as Action

@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var validateUsernameUseCase: ValidateUsernameUseCase
    private lateinit var uiState: MutableStateFlow<CreateProfileUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        navController = TestNavHostController(context)
        viewModel = mockk(relaxed = true)
        validateUsernameUseCase = ValidateUsernameUseCase()
        uiState = MutableStateFlow(CreateProfileUIState())
        uiEventFlow = MutableSharedFlow()
        windowSizeClass = WindowSizeClass.calculateFromSize(
            DpSize(width = 400.dp, height = 800.dp),
            setOf(WindowWidthSizeClass.Compact),
            setOf(WindowHeightSizeClass.Compact)
        )

        every { viewModel.state } returns uiState
        every { viewModel.uiEvent } returns uiEventFlow as SharedFlow<UIEvent>
        every { viewModel.isScreenVisible } returns MutableStateFlow(true)

        composeTestRule.setContent {
            LearnFlexTheme {
                CompositionLocalProvider {
                    CreateProfileScreen(
                        navController = navController,
                        viewModel = viewModel,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }

    @Test
    fun createProfileScreenDisplaysCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_IMAGE_UPLOAD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_FIELD_PICKER.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun usernameTextField_calls_viewModel_onUsernameChanged_whenTextEntered() {
        val username = "TestUser"
        every { viewModel.handleAction(Action.EditUsername(username)) } answers {
            with(validateUsernameUseCase(username)) {
                when (this@with) {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update { it.copy(username = username, usernameError = message) }
                }
            }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.handleAction(Action.EditUsername(username)) }
        composeTestRule.onNodeWithText(username).assertIsDisplayed()
    }

    @Test
    fun usernameTextField_displaysError_whenInvalidUsernameEntered() {
        val username = "Test User"
        val errorMessage = (validateUsernameUseCase(username) as ValidationResult.Invalid).message
        every { viewModel.handleAction(Action.EditUsername(username)) } answers {
            with(validateUsernameUseCase(username)) {
                when (this@with) {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update {
                        it.copy(username = username, usernameError = message)
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.handleAction(Action.EditUsername(username)) }
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun goalTextField_calls_viewModel_onGoalChanged_whenTextEntered() {
        val goal = "TestGoal"
        every { viewModel.handleAction(Action.EditGoal(goal)) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).performTextInput(goal)

        verify { viewModel.handleAction(Action.EditGoal(goal)) }
        composeTestRule.onNodeWithText(goal).assertIsDisplayed()
    }

    @Test
    fun goalTextField_displays_character_count() {
        val goal = "TestGoal"
        every { viewModel.handleAction(Action.EditGoal(goal)) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).performTextInput(goal)

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_CHAR_COUNTER.tag, true)
            .assertTextContains(value = goal.length.toString(), substring = true)
    }

    @Test
    fun levelDropdown_calls_viewModel_onLevelChanged_whenSelected() {
        val level = Level.ADVANCED
        every {
            viewModel.handleAction(Action.ToggleLevelDropdownVisibility)
        } answers {
            uiState.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }
        }
        every { viewModel.handleAction(Action.SelectLevel(level)) } answers {
            uiState.update { it.copy(level = level) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN_BUTTON.tag).performClick()
        verify { viewModel.handleAction(Action.ToggleLevelDropdownVisibility) }

        composeTestRule.onNodeWithText(Level.ADVANCED.value).performClick()
        verify { viewModel.handleAction(Action.SelectLevel(level)) }

        composeTestRule.onNodeWithText(Level.ADVANCED.value).assertIsDisplayed()
    }

    @Test
    fun fieldPicker_calls_viewModel_onFieldChanged_whenScrolled() {
        composeTestRule.onNodeWithTag("${Field::class.simpleName}_picker")
            .performTouchInput {
                down(Offset(10f, 0f))
                moveTo(Offset(10f, -100f))
                up()
            }

        verify {
            viewModel.handleAction(
                match {
                    it is Action.SelectField
                }
            )
        }
    }

    @Test
    fun createProfileButton_calls_viewModel_onCreateProfile_whenClicked() {
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).performClick()

        verify {
            viewModel.handleAction(
                match {
                    it is Action.CreateProfile
                }
            )
        }
    }

    @Test
    fun personalInfoForm_disables_buttons_and_fields_when_loading() {
        uiState.update { it.copy(isLoading = true) }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag("${Level::class.simpleName}_dropdown_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun styleQuestionnaireForm_displays_correctly() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE.tag).assertIsEnabled()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_OPTIONS_GROUP.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun styleQuestionnaireOptionsGroup_selects_option_whenClicked() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        composeTestRule.onNodeWithText(Style.READING.value).performClick()

        composeTestRule.onNodeWithText(Style.READING.value).assertIsSelected()
    }

    @Test
    fun styleQuestionnaireOptionsGroup_calls_viewModel_onQuestionAnswered_whenNextButtonClicked() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        composeTestRule.onNodeWithText(Style.READING.value).performClick()
        composeTestRule.onNodeWithText(Style.READING.value).assertIsSelected()

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag).performClick()
        verify {
            viewModel.handleAction(
                match {
                    it is Action.HandleQuestionAnswered
                }
            )
        }
    }

    @Test
    fun styleQuestionnaireResultDialog_displays_correctly() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_RESULT_DIALOG.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_BREAKDOWN.tag).assertIsDisplayed()
    }

    @Test
    fun styleQuestionnaireResultDialog_calls_viewModel_setLearningStyle_whenButtonClicked() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag).performClick()

        verify {
            viewModel.handleAction(
                match {
                    it is Action.SetLearningStyle
                }
            )
        }
    }

    @Test
    fun styleQuestionnaireResultDialog_calls_viewModel_startStyleQuestionnaire_whenButtonClicked() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.StyleQuestionnaire,
                styleQuestionnaire = styleQuestionnaire,
                learningStyle = learningStyle,
                showStyleResultDialog = true
            )
        }

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_RESTART_BUTTON.tag).performClick()

        verify { viewModel.handleAction(Action.StartStyleQuestionnaire) }
    }

    companion object {
        private val styleQuestionnaire = listOf(
            StyleQuizGeneratorClient.StyleQuestion(
                listOf(StyleQuizGeneratorClient.StyleQuestion.StyleOption(Style.READING.value, Style.READING.value)), "Question 1",
            )
        )

        private val learningStyle = Profile.LearningStyle(
            dominant = Style.READING.value,
            breakdown = Profile.LearningStyleBreakdown(reading = 50, kinesthetic = 50)
        )
    }
}

