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
import org.example.shared.data.model.Level
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult
import org.example.shared.presentation.viewModel.CreateUserProfileViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateProfileScreenTest
{
    private lateinit var navController: NavController
    private lateinit var viewModel: CreateUserProfileViewModel
    private lateinit var uiState: MutableStateFlow<CreateProfileUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @BeforeTest
    fun setUp()
    {
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
        onNodeWithTag("imageUpload").assertIsDisplayed()
        onNodeWithTag("usernameTextField").assertIsDisplayed()
        onNodeWithTag("goalTextField").assertIsDisplayed()
        onNodeWithTag("levelDropdown").assertIsDisplayed()
        onNodeWithTag("fieldPicker").assertIsDisplayed()
        onNodeWithTag("createProfileButton").assertIsDisplayed()
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
                when (this@with)
                {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update { it.copy(username = username, usernameError = message) }
                }
            }
        }

        onNodeWithTag("usernameTextField").performTextInput(username)

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
                when (this@with)
                {
                    is ValidationResult.Valid -> uiState.update { it.copy(username = value, usernameError = null) }
                    is ValidationResult.Invalid -> uiState.update { it.copy(username = username, usernameError = message) }
                }
            }
        }

        onNodeWithTag("usernameTextField").performTextInput(username)

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

        onNodeWithTag("goalTextField").performTextInput(goal)

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

        onNodeWithTag("${Level::class.simpleName}_dropdown_button").performClick()
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
        onNodeWithTag("Picker")
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
        onNodeWithTag("createProfileButton").performClick()

        verify { viewModel.onCreateProfile(any()) }
    }
}