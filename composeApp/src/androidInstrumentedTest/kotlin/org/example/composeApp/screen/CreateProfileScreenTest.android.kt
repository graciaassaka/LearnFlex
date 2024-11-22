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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var uiState: MutableStateFlow<CreateProfileUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        navController = TestNavHostController(context)
        viewModel = mockk(relaxed = true)
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
                CompositionLocalProvider(LocalComposition.MaxFileSize provides 1024L) {
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
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with) {
                    is ValidationResult.Valid   -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update {
                        it.copy(
                            username = username,
                            usernameError = message
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        composeTestRule.onNodeWithText(username).assertIsDisplayed()
    }

    @Test
    fun usernameTextField_displaysError_whenInvalidUsernameEntered() {
        val username = "Test User"
        val errorMessage = (InputValidator.validateUsername(username) as ValidationResult.Invalid).message
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with) {
                    is ValidationResult.Valid   -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update {
                        it.copy(
                            username = username,
                            usernameError = message
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_USERNAME_TEXT_FIELD.tag).performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun goalTextField_calls_viewModel_onGoalChanged_whenTextEntered() {
        val goal = "TestGoal"
        every { viewModel.onGoalChanged(goal) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).performTextInput(goal)

        verify { viewModel.onGoalChanged(goal) }
        composeTestRule.onNodeWithText(goal).assertIsDisplayed()
    }

    @Test
    fun goalTextField_displays_character_count() {
        val goal = "TestGoal"
        every { viewModel.onGoalChanged(goal) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_TEXT_FIELD.tag).performTextInput(goal)

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_GOAL_CHAR_COUNTER.tag, true)
            .assertTextContains(value = goal.length.toString(), substring = true)
    }

    @Test
    fun levelDropdown_calls_viewModel_onLevelChanged_whenSelected() {
        val level = Level.Advanced
        every {
            viewModel.toggleLevelDropdownVisibility()
        } answers {
            uiState.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }
        }
        every { viewModel.onLevelChanged(level) } answers {
            uiState.update { it.copy(level = level) }
        }

        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_LEVEL_DROPDOWN_BUTTON.tag).performClick()
        verify { viewModel.toggleLevelDropdownVisibility() }

        composeTestRule.onNodeWithText(Level.Advanced.name).performClick()
        verify { viewModel.onLevelChanged(level) }

        composeTestRule.onNodeWithText("Advanced").assertIsDisplayed()
    }

    @Test
    fun fieldPicker_calls_viewModel_onFieldChanged_whenScrolled() {
        composeTestRule.onNodeWithTag("${Field::class.simpleName}_picker")
            .performTouchInput {
                down(Offset(10f, 0f))
                moveTo(Offset(10f, -100f))
                up()
            }

        verify { viewModel.onFieldChanged(any()) }
    }

    @Test
    fun createProfileButton_calls_viewModel_onCreateProfile_whenClicked() {
        composeTestRule.onNodeWithTag(TestTags.PERSONAL_INFO_CREATE_PROFILE_BUTTON.tag).performClick()

        verify { viewModel.onCreateProfile(any()) }
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
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
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
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
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
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire
            )
        }

        composeTestRule.onNodeWithText(Style.READING.value).performClick()
        composeTestRule.onNodeWithText(Style.READING.value).assertIsSelected()

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_NEXT_BUTTON.tag).performClick()
        verify { viewModel.onQuestionAnswered(any()) }
    }

    @Test
    fun styleQuestionnaireResultDialog_displays_correctly() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                styleResult = styleResult,
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
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                styleResult = styleResult,
                showStyleResultDialog = true
            )
        }

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_SET_LEARNING_STYLE_BUTTON.tag).performClick()

        verify { viewModel.setLearningStyle(any()) }
    }

    @Test
    fun styleQuestionnaireResultDialog_calls_viewModel_startStyleQuestionnaire_whenButtonClicked() {
        uiState.update {
            it.copy(
                currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE,
                styleQuestionnaire = styleQuestionnaire,
                styleResult = styleResult,
                showStyleResultDialog = true
            )
        }

        composeTestRule.onNodeWithTag(TestTags.STYLE_QUESTIONNAIRE_RESTART_BUTTON.tag).performClick()

        verify { viewModel.startStyleQuestionnaire() }
    }

    companion object {
        private val styleQuestionnaire = listOf(
                StyleQuestion(
                    listOf(StyleOption(Style.READING.value, Style.READING.value)), "Question 1",
                )
            )

        private val styleResult = StyleResult(
            dominantStyle = Style.READING.value,
            styleBreakdown = StyleBreakdown(visual = 0, reading = 50, kinesthetic = 50)
        )
    }
}

