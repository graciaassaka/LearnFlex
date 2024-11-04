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
import org.example.shared.data.model.Level
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateProfileScreenTest
{

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var uiState: MutableStateFlow<CreateProfileUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp()
    {
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
    fun createProfileScreenDisplaysCorrectly()
    {
        composeTestRule.onNodeWithTag("imageUpload").assertIsDisplayed()
        composeTestRule.onNodeWithTag("usernameTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("goalTextField").assertIsDisplayed()
        composeTestRule.onNodeWithTag("levelDropdown").assertIsDisplayed()
        composeTestRule.onNodeWithTag("fieldPicker").assertIsDisplayed()
        composeTestRule.onNodeWithTag("createProfileButton").assertIsDisplayed()
    }

    @Test
    fun usernameTextField_calls_viewModel_onUsernameChanged_whenTextEntered()
    {
        val username = "TestUser"
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with)
                {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update { it.copy(username = username, usernameError = message) }
                }
            }
        }

        composeTestRule.onNodeWithTag("usernameTextField").performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        composeTestRule.onNodeWithText(username).assertIsDisplayed()
    }

    @Test
    fun usernameTextField_displaysError_whenInvalidUsernameEntered()
    {
        val username = "Test User"
        val errorMessage = (InputValidator.validateUsername(username) as ValidationResult.Invalid).message
        every { viewModel.onUsernameChanged(username) } answers {
            with(InputValidator.validateUsername(username)) {
                when (this@with)
                {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update { it.copy(username = username, usernameError = message) }
                }
            }
        }

        composeTestRule.onNodeWithTag("usernameTextField").performTextInput(username)

        verify { viewModel.onUsernameChanged(username) }
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun goalTextField_calls_viewModel_onGoalChanged_whenTextEntered()
    {
        val goal = "TestGoal"
        every { viewModel.onGoalChanged(goal) } answers {
            uiState.update { it.copy(goal = goal) }
        }

        composeTestRule.onNodeWithTag("goalTextField").performTextInput(goal)

        verify { viewModel.onGoalChanged(goal) }
        composeTestRule.onNodeWithText(goal).assertIsDisplayed()
    }

    @Test
    fun levelDropdown_calls_viewModel_onLevelChanged_whenSelected()
    {
        val level = Level.Advanced
        every {
            viewModel.toggleLevelDropdownVisibility()
        } answers {
            uiState.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }
        }
        every { viewModel.onLevelChanged(level) } answers {
            uiState.update { it.copy(level = level) }
        }

        composeTestRule.onNodeWithTag("${ Level::class.simpleName }_dropdown_button").performClick()
        verify { viewModel.toggleLevelDropdownVisibility() }

        composeTestRule.onNodeWithText(Level.Advanced.name).performClick()
        verify { viewModel.onLevelChanged(level) }

        composeTestRule.onNodeWithText("Advanced").assertIsDisplayed()
    }

    @Test
    fun fieldPicker_calls_viewModel_onFieldChanged_whenScrolled()
    {
        composeTestRule.onNodeWithTag("Picker")
            .performTouchInput {
                down(Offset(10f, 0f))
                moveTo(Offset(10f, -100f))
                up()
            }

        verify { viewModel.onFieldChanged(any()) }
    }

    @Test
    fun createProfileButton_calls_viewModel_onCreateProfile_whenClicked()
    {
        composeTestRule.onNodeWithTag("createProfileButton").performClick()

        verify { viewModel.onCreateProfile(any()) }
    }
}

