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
import org.example.composeApp.theme.LearnFlexTheme
import org.example.shared.presentation.state.AuthUIState
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.presentation.util.AuthForm
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AuthScreenTest
{

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var viewModel: AuthViewModel
    private lateinit var uiState: MutableStateFlow<AuthUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Before
    fun setUp()
    {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        navController = TestNavHostController(context)
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(AuthUIState())
        uiEventFlow = MutableSharedFlow()
        windowSizeClass = WindowSizeClass.calculateFromSize(
            DpSize(width = 800.dp, height = 800.dp),
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
    fun signInForm_displaysCorrectly()
    {
        composeTestRule.onNodeWithTag("sign_in_screen_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_forgot_password_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_create_account_button").assertIsDisplayed()
    }

    @Test
    fun signInForm_updateEmailField()
    {
        // Given
        val email = "test@example.com"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignInEmailChanged(email) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signInEmail = email, signInEmailError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_email_field").performTextInput(email)

        // Then
        verify { viewModel.onSignInEmailChanged(email) }
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    @Test
    fun signInForm_displaysErrorWhenEmailIsInvalid()
    {
        // Given
        val email = "test@example"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignInEmailChanged(email) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signInEmail = email, signInEmailError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_email_field").performTextInput(email)

        // Then
        verify { viewModel.onSignInEmailChanged(email) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signInForm_updatePasswordField()
    {
        // Given
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignInPasswordChanged(password) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signInPassword = password, signInPasswordError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_password_field").performTextInput(password)

        // Then
        verify { viewModel.onSignInPasswordChanged(password) }
    }

    @Test
    fun signInForm_displaysErrorWhenPasswordIsInvalid()
    {
        // Given
        val password = "password"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignInPasswordChanged(password) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signInPassword = password, signInPasswordError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_password_field").performTextInput(password)

        // Then
        verify { viewModel.onSignInPasswordChanged(password) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signInForm_togglePasswordVisibility()
    {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password) }

        every { viewModel.toggleSignInPasswordVisibility() } answers {
            uiState.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_password_field").performTextInput(password)
        composeTestRule.onNodeWithTag("toggle_password_visibility").performClick()

        // Then
        verify { viewModel.toggleSignInPasswordVisibility() }
    }

    @Test
    fun signInForm_signInWhenEmailAndPasswordAreValid()
    {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val emailValidationResult = InputValidator.validateEmail(email)
        val passwordValidationResult = InputValidator.validatePassword(password)

        every { viewModel.onSignInEmailChanged(email) } answers {
            uiState.update {
                when (emailValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signInEmail = email, signInEmailError = emailValidationResult.message)
                }
            }
        }

        every { viewModel.onSignInPasswordChanged(password) } answers {
            uiState.update {
                when (passwordValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signInPassword = password, signInPasswordError = passwordValidationResult.message)
                }
            }
        }

        every { viewModel.signIn() } answers {
            uiState.update { it.copy(isLoading = true) }
            uiState.update { it.copy(isLoading = false) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_email_field").performTextInput(email)
        composeTestRule.onNodeWithTag("sign_in_password_field").performTextInput(password)
        composeTestRule.onNodeWithTag("sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.signIn() }
    }

    @Test
    fun signInFrom_disablesSignInButtonWhenEmailIsInvalid()
    {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password, signInPasswordError = null) }

        val email = "test@example"
        val emailValidationResult = InputValidator.validateEmail(email)

        every { viewModel.onSignInEmailChanged(email) } answers {
            uiState.update {
                when (emailValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInEmail = email, signInEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signInEmail = email, signInEmailError = emailValidationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_email_field").performTextInput(email)

        // Then
        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()
    }

    @Test
    fun signInForm_disablesSignInButtonWhenPasswordIsInvalid()
    {
        // Given
        val email = "test@example.com"
        uiState.update { it.copy(signInEmail = email, signInEmailError = null) }

        val password = "password"
        val passwordValidationResult = InputValidator.validatePassword(password)

        every { viewModel.onSignInPasswordChanged(password) } answers {
            uiState.update {
                when (passwordValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signInPassword = password, signInPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signInPassword = password, signInPasswordError = passwordValidationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_password_field").performTextInput(password)

        // Then
        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()
    }

    @Test
    fun signInForm_disableButtonsWhenUIStateIsLoading()
    {
        // Given
        uiState.update { it.copy(isLoading = true) }


        // Then
        composeTestRule.onNodeWithTag("sign_in_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("sign_in_forgot_password_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("sign_in_create_account_button").assertIsNotEnabled()
    }

    @Test
    fun signInForm_navigatesToForgotPasswordForm()
    {
        // Given
        every { viewModel.displayAuthForm(AuthForm.ForgotPassword) } answers {
            uiState.update { it.copy(currentForm = AuthForm.ForgotPassword) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_forgot_password_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.ForgotPassword) }
    }

    @Test
    fun signInForm_navigatesToSignUpForm()
    {
        // Given
        every { viewModel.displayAuthForm(AuthForm.SignUp) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_in_create_account_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignUp) }
        composeTestRule.onNodeWithTag("sign_up_form").assertIsDisplayed()
    }

    @Test
    fun signUpForm_displaysCorrectly()
    {
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        composeTestRule.onNodeWithTag("sign_up_screen_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_email_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_confirm_password_field").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_up_sign_in_button").assertIsDisplayed()
    }

    @Test
    fun signUpForm_updateEmailField()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignUpEmailChanged(email) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signUpEmail = email, signUpEmailError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_email_field").performTextInput(email)

        // Then
        verify { viewModel.onSignUpEmailChanged(email) }
        composeTestRule.onNodeWithText(email).assertIsDisplayed()
    }

    @Test
    fun signUpForm_displaysErrorWhenEmailIsInvalid()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignUpEmailChanged(email) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signUpEmail = email, signUpEmailError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_email_field").performTextInput(email)

        // Then
        verify { viewModel.onSignUpEmailChanged(email) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_updatePasswordField()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignUpPasswordChanged(password) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signUpPassword = password, signUpPasswordError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_password_field").performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordChanged(password) }
    }

    @Test
    fun signUpForm_displaysErrorWhenPasswordIsInvalid()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignUpPasswordChanged(password) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signUpPassword = password, signUpPasswordError = validationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_password_field").performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordChanged(password) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_updateConfirmPasswordField()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePasswordConfirmation(password, password)
        every { viewModel.onSignUpPasswordConfirmationChanged(password) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPasswordConfirmation = password, signUpPasswordConfirmationError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = password,
                        signUpPasswordConfirmationError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_confirm_password_field").performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordConfirmationChanged(password) }
    }

    @Test
    fun signUpForm_displaysErrorWhenConfirmPasswordIsInvalid()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val confirmPassword = "password123"
        val validationResult = InputValidator.validatePasswordConfirmation(password, confirmPassword)
        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
            uiState.update {
                when (validationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPasswordConfirmation = confirmPassword, signUpPasswordConfirmationError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = validationResult.message
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_confirm_password_field").performTextInput(confirmPassword)

        // Then
        verify { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) }
        composeTestRule.onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @Test
    fun signUpForm_displayVerifyEmailFormWhenEmailAndPasswordAreValid()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val confirmPassword = "P@ssw0rd"
        val emailValidationResult = InputValidator.validateEmail(email)
        val passwordValidationResult = InputValidator.validatePassword(password)
        val confirmPasswordValidationResult = InputValidator.validatePasswordConfirmation(password, confirmPassword)

        every { viewModel.onSignUpEmailChanged(email) } answers {
            uiState.update {
                when (emailValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signUpEmail = email, signUpEmailError = emailValidationResult.message)
                }
            }
        }

        every { viewModel.onSignUpPasswordChanged(password) } answers {
            uiState.update {
                when (passwordValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signUpPassword = password, signUpPasswordError = passwordValidationResult.message)
                }
            }
        }

        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
            uiState.update {
                when (confirmPasswordValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPasswordConfirmation = confirmPassword, signUpPasswordConfirmationError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpPasswordConfirmation = confirmPassword,
                        signUpPasswordConfirmationError = confirmPasswordValidationResult.message
                    )
                }
            }
        }

        every { viewModel.signUp() } answers {
            uiState.update { it.copy(isLoading = true) }
            uiState.update { it.copy(currentForm = AuthForm.VerifyEmail) }
            uiState.update { it.copy(isLoading = false) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_email_field").performTextInput(email)
        composeTestRule.onNodeWithTag("sign_up_password_field").performTextInput(password)
        composeTestRule.onNodeWithTag("sign_up_confirm_password_field").performTextInput(confirmPassword)
        composeTestRule.onNodeWithTag("sign_up_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.signUp() }
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenEmailIsInvalid()
    {
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
        val emailValidationResult = InputValidator.validateEmail(email)

        every { viewModel.onSignUpEmailChanged(email) } answers {
            uiState.update {
                when (emailValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(signUpEmail = email, signUpEmailError = emailValidationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_email_field").performTextInput(email)

        // Then
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenPasswordIsInvalid()
    {
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
        val passwordValidationResult = InputValidator.validatePassword(password)

        every { viewModel.onSignUpPasswordChanged(password) } answers {
            uiState.update {
                when (passwordValidationResult)
                {
                    is ValidationResult.Valid -> it.copy(signUpPassword = password, signUpPasswordError = null)
                    is ValidationResult.Invalid -> it.copy(signUpPassword = password, signUpPasswordError = passwordValidationResult.message)
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_password_field").performTextInput(password)

        // Then
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disablesSignUpButtonWhenConfirmPasswordIsInvalid()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val password = "P@ssw0rd"
        uiState.update { it.copy(signUpEmail = email, signUpEmailError = null, signUpPassword = password, signUpPasswordError = null) }

        val confirmPassword = "password123"
        val confirmPasswordValidationResult = InputValidator.validatePasswordConfirmation(password, confirmPassword)

        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
            uiState.update {
                when (confirmPasswordValidationResult)
                {
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
        composeTestRule.onNodeWithTag("sign_up_confirm_password_field").performTextInput(confirmPassword)

        // Then
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
    }

    @Test
    fun signUpForm_disableButtonsWhenUIStateIsLoading()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp, isLoading = true) }

        // Then
        composeTestRule.onNodeWithTag("sign_up_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("sign_up_sign_in_button").assertIsNotEnabled()
    }

    @Test
    fun signUpForm_navigatesToSignInForm()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        every { viewModel.displayAuthForm(AuthForm.SignIn) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignIn) }
        }

        // When
        composeTestRule.onNodeWithTag("sign_up_sign_in_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignIn) }
        composeTestRule.onNodeWithTag("sign_in_form").assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_displaysCorrectly()
    {
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, signUpEmail = "test@example.com") }
        composeTestRule.onNodeWithTag("verify_email_screen_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verify_email_screen_description").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verify_email_screen_email").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verify_email_screen_edit_email_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verify_email_screen_verify_email_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("verify_email_resend_email_button").assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_navigatesToSignUpForm()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail) }
        every { viewModel.displayAuthForm(AuthForm.SignUp) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        composeTestRule.onNodeWithTag("verify_email_screen_edit_email_button").performClick()
        composeTestRule.waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignUp) }
        verify { viewModel.deleteUser() }
        composeTestRule.onNodeWithTag("sign_up_form").assertIsDisplayed()
    }

    @Test
    fun verifyEmailForm_disableButtonsWhenUIStateIsLoading()
    {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, isLoading = true) }

        // Then
        composeTestRule.onNodeWithTag("verify_email_screen_verify_email_button").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("verify_email_resend_email_button").assertIsNotEnabled()
    }
}