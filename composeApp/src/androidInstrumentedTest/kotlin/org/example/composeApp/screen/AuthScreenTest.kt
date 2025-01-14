package org.example.composeApp.screen

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import org.example.composeApp.presentation.action.AuthAction
import org.example.composeApp.presentation.state.AuthUIState
import org.example.composeApp.presentation.state.AuthUIState.AuthForm
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.screen.AuthScreen
import org.example.composeApp.presentation.ui.theme.LearnFlexTheme
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.AuthViewModel
import org.example.shared.domain.use_case.validation.ValidateEmailUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordConfirmationUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: AuthViewModel
    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase
    private lateinit var validatePasswordConfirmationUseCase: ValidatePasswordConfirmationUseCase
    private lateinit var uiState: MutableStateFlow<AuthUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        navController = TestNavHostController(context)
        viewModel = mockk(relaxed = true)
        validateEmailUseCase = ValidateEmailUseCase()
        validatePasswordUseCase = ValidatePasswordUseCase()
        validatePasswordConfirmationUseCase = ValidatePasswordConfirmationUseCase()
        uiState = MutableStateFlow(AuthUIState())
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
                AuthScreen(
                    windowSizeClass = windowSizeClass,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun signInForm_displaysCorrectly() {
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_SCREEN_TITLE.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun signInForm_updateEmailField() {
        // Given
        val email = "test@example.com"
        val validationResult = validateEmailUseCase(email)
        every { viewModel.handleAction(AuthAction.EditSignInEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInEmail = email,
                        signInEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignInEmail(email)) }
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    @Test
    fun signInForm_displaysErrorWhenEmailIsInvalid() {
        // Given
        val email = "test@example"
        val validationResult = validateEmailUseCase(email)
        every { viewModel.handleAction(AuthAction.EditSignInEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInEmail = email,
                        signInEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignInEmail(email)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signInForm_updatePasswordField() {
        // Given
        val password = "P@ssw0rd"
        val validationResult = validatePasswordUseCase(password)
        every { viewModel.handleAction(AuthAction.EditSignInPassword(password)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInPassword = password,
                        signInPasswordError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignInPassword(password)) }
    }

    @Test
    fun signInForm_displaysErrorWhenPasswordIsInvalid() {
        // Given
        val password = "password"
        val validationResult = validatePasswordUseCase(password)
        every { viewModel.handleAction(AuthAction.EditSignInPassword(password)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInPassword = password,
                        signInPasswordError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignInPassword(password)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signInForm_togglePasswordVisibility() {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password) }

        every { viewModel.handleAction(AuthAction.ToggleSignInPasswordVisibility) } answers {
            uiState.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)
        composeTestRule.onNodeWithTag(TestTags.TOGGLE_PASSWORD_VISIBILITY.tag).performClick()

        // Then
        verify { viewModel.handleAction(AuthAction.ToggleSignInPasswordVisibility) }
    }

    @Test
    fun signInForm_signInWhenEmailAndPasswordAreValid() {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val emailValidationResult = validateEmailUseCase(email)
        val passwordValidationResult = validatePasswordUseCase(password)

        every { viewModel.handleAction(AuthAction.EditSignInEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInEmail = email,
                        signInEmailError = emailValidationResult.message
                    )
                }
            }
        }

        every { viewModel.handleAction(AuthAction.EditSignInPassword(password)) } answers {
            uiState.update {
                when (passwordValidationResult) {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInPassword = password,
                        signInPasswordError = passwordValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify {
            viewModel.handleAction(
                match {
                    it is AuthAction.SignIn
                }
            )
        }
    }

    @Test
    fun signInFrom_disablesSignInButtonWhenEmailIsInvalid() {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password, signInPasswordError = null) }

        val email = "test@example"
        val emailValidationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditSignInEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInEmail = email,
                        signInEmailError = emailValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signInForm_disablesSignInButtonWhenPasswordIsInvalid() {
        // Given
        val email = "test@example.com"
        uiState.update { it.copy(signInEmail = email, signInEmailError = null) }

        val password = "password"
        val passwordValidationResult = validatePasswordUseCase(password)

        every { viewModel.handleAction(AuthAction.EditSignInPassword(password)) } answers {
            uiState.update {
                when (passwordValidationResult) {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signInPassword = password,
                        signInPasswordError = passwordValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signInForm_disableButtonsWhenUIStateIsLoading() {
        // Given
        uiState.update { it.copy(isLoading = true) }


        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signInForm_navigatesToForgotPasswordForm() {
        // Given
        every { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.ResetPassword)) } answers {
            uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.ResetPassword)) }
    }

    @Test
    fun signInForm_navigatesToSignUpForm() {
        // Given
        every { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignUp)) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignUp)) }
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_FORM.tag).assertIsDisplayed()
    }

    @Test
    fun signUpForm_displaysCorrectly() {
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_SCREEN_TITLE.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun signUpForm_updateEmailField() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val validationResult = validateEmailUseCase(email)
        every { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpEmail = email,
                        signUpEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) }
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    @Test
    fun signUpForm_displaysErrorWhenEmailIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example"
        val validationResult = validateEmailUseCase(email)
        every { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpEmail = email,
                        signUpEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_updatePasswordField() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = validatePasswordUseCase(password)
        every { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPassword = password,
                        signUpPasswordError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) }
    }

    @Test
    fun signUpForm_displaysErrorWhenPasswordIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val validationResult = validatePasswordUseCase(password)
        every { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPassword = password,
                        signUpPasswordError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_updateConfirmPasswordField() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = validatePasswordConfirmationUseCase(password, password)
        every { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(password)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(
                        signUpPasswordConfirmation = password,
                        signUpPasswordConfirmationError = null
                    )

                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = password,
                        signUpPasswordConfirmationError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(password)) }
    }

    @Test
    fun signUpForm_displaysErrorWhenConfirmPasswordIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val confirmPassword = "password123"
        val validationResult = validatePasswordConfirmationUseCase(password, confirmPassword)
        every { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(confirmPassword)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = null
                    )

                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)

        // Then
        verify { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(confirmPassword)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_displayVerifyEmailFormWhenEmailAndPasswordAreValid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val confirmPassword = "P@ssw0rd"
        val emailValidationResult = validateEmailUseCase(email)
        val passwordValidationResult = validatePasswordUseCase(password)
        val confirmPasswordValidationResult = validatePasswordConfirmationUseCase(password, confirmPassword)

        every { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpEmail = email,
                        signUpEmailError = emailValidationResult.message
                    )
                }
            }
        }

        every { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) } answers {
            uiState.update {
                when (passwordValidationResult) {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPassword = password,
                        signUpPasswordError = passwordValidationResult.message
                    )
                }
            }
        }

        every { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(confirmPassword)) } answers {
            uiState.update {
                when (confirmPasswordValidationResult) {
                    is ValidationResult.Valid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = null
                    )

                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = confirmPasswordValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify {
            viewModel.handleAction(
                match {
                    it is AuthAction.SignUp
                }
            )
        }
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenEmailIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val confirmPassword = "P@ssw0rd"
        uiState.update {
            it.copy(
                signUpPassword = password,
                signUpPasswordConfirmation = confirmPassword,
                signUpPasswordError = null,
                signUpPasswordConfirmationError = null
            )
        }

        val email = "test@example"
        val emailValidationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditSignUpEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpEmail = email,
                        signUpEmailError = emailValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenPasswordIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val confirmPassword = "P@ssw0rd"
        uiState.update {
            it.copy(
                signUpEmail = email,
                signUpEmailError = null,
                signUpPasswordConfirmation = confirmPassword,
                signUpPasswordConfirmationError = null
            )
        }

        val password = "password"
        val passwordValidationResult = validatePasswordUseCase(password)

        every { viewModel.handleAction(AuthAction.EditSignUpPassword(password)) } answers {
            uiState.update {
                when (passwordValidationResult) {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPassword = password,
                        signUpPasswordError = passwordValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenConfirmPasswordIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val password = "P@ssw0rd"
        uiState.update {
            it.copy(
                signUpEmail = email,
                signUpEmailError = null,
                signUpPassword = password,
                signUpPasswordError = null
            )
        }

        val confirmPassword = "password123"
        val confirmPasswordValidationResult = validatePasswordConfirmationUseCase(password, confirmPassword)

        every { viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(confirmPassword)) } answers {
            uiState.update {
                when (confirmPasswordValidationResult) {
                    is ValidationResult.Valid ->
                        it.copy(signUpPasswordConfirmation = confirmPassword, signUpPasswordConfirmationError = null)

                    is ValidationResult.Invalid ->
                        it.copy(
                            signUpPasswordConfirmation = confirmPassword,
                            signUpPasswordConfirmationError = confirmPasswordValidationResult.message
                        )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disableButtonsWhenUIStateIsLoading() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp, isLoading = true) }

        // Then
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun signUpForm_navigatesToSignInForm() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        every { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignIn)) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignIn) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignIn)) }
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_FORM.tag).assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_displaysCorrectly() {
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, signUpEmail = "test@example.com") }
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_TITLE.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_DESCRIPTION.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EMAIL.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EDIT_EMAIL_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_VERIFY_EMAIL_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_RESEND_EMAIL_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_navigatesToSignUpForm() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail) }
        every { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignUp)) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EDIT_EMAIL_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignUp)) }
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignUp)) }
        composeTestRule.onNodeWithTag(TestTags.SIGN_UP_FORM.tag).assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_disableButtonsWhenUIStateIsLoading() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, isLoading = true) }

        // Then
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_VERIFY_EMAIL_BUTTON.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.VERIFY_EMAIL_RESEND_EMAIL_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun passwordResetForm_displaysCorrectly() {
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_SCREEN_TITLE.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).assertIsDisplayed()
    }

    @Test
    fun passwordResetForm_updateEmailField() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example.com"
        val validationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(resetPasswordEmail = email, resetPasswordEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        resetPasswordEmail = email,
                        resetPasswordEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) }
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    @Test
    fun passwordResetForm_displaysErrorWhenEmailIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example"
        val validationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) } answers {
            uiState.update {
                when (validationResult) {
                    is ValidationResult.Valid -> it.copy(resetPasswordEmail = email, resetPasswordEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        resetPasswordEmail = email,
                        resetPasswordEmailError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun passwordResetForm_disableButtonsWhenUIStateIsLoading() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword, isLoading = true) }

        // Then
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @Test
    fun passwordResetForm_navigatesToSignInForm() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        every { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignIn)) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignIn) }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.handleAction(AuthAction.DisplayAuthForm(AuthForm.SignIn)) }
        composeTestRule.onNodeWithTag(TestTags.SIGN_IN_FORM.tag).assertIsDisplayed()
    }

    @Test
    fun passwordResetForm_callsSendPasswordResetEmailWhenEmailIsValid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example.com"
        val emailValidationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(resetPasswordEmail = email, resetPasswordEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        resetPasswordEmail = email,
                        resetPasswordEmailError = emailValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).performClick()
        composeTestRule.waitForIdle()

        // Then
        verify {
            viewModel.handleAction(
                match {
                    it is AuthAction.SendPasswordResetEmail
                }
            )
        }
    }

    @Test
    fun passwordResetForm_disablesResetButtonWhenEmailIsInvalid() {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example"
        val emailValidationResult = validateEmailUseCase(email)

        every { viewModel.handleAction(AuthAction.EditPasswordResetEmail(email)) } answers {
            uiState.update {
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(resetPasswordEmail = email, resetPasswordEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        resetPasswordEmail = email,
                        resetPasswordEmailError = emailValidationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        composeTestRule.onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsNotEnabled()
    }
}