package org.example.composeApp.screen

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import org.example.composeApp.util.TestTags
import org.example.shared.presentation.state.AuthUIState
import org.example.shared.presentation.util.AuthForm
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult
import org.example.shared.presentation.viewModel.AuthViewModel
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthScreenTest {
    private lateinit var navController: NavController
    private lateinit var viewModel: AuthViewModel
    private lateinit var uiState: MutableStateFlow<AuthUIState>
    private lateinit var uiEventFlow: MutableSharedFlow<UIEvent>
    private lateinit var windowSizeClass: WindowSizeClass

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @BeforeTest
    fun setUp() {
        navController = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)
        uiState = MutableStateFlow(AuthUIState())
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
    fun signInForm_displaysCorrectly() = runComposeUiTest {
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_SCREEN_TITLE.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_updateEmailField() = runComposeUiTest {
        // Given
        val email = "test@example.com"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignInEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onSignInEmailChanged(email) }
        onNodeWithText(email).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_displaysErrorWhenEmailIsInvalid() = runComposeUiTest {
        // Given
        val email = "test@example"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignInEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onSignInEmailChanged(email) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_updatePasswordField() = runComposeUiTest {
        // Given
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignInPasswordChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.onSignInPasswordChanged(password) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_displaysErrorWhenPasswordIsInvalid() = runComposeUiTest {
        // Given
        val password = "password"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignInPasswordChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.onSignInPasswordChanged(password) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_togglePasswordVisibility() = runComposeUiTest {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password) }

        every { viewModel.toggleSignInPasswordVisibility() } answers {
            uiState.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)
        onNodeWithTag(TestTags.TOGGLE_PASSWORD_VISIBILITY.tag).performClick()

        // Then
        verify { viewModel.toggleSignInPasswordVisibility() }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_signInWhenEmailAndPasswordAreValid() = runComposeUiTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val emailValidationResult = InputValidator.validateEmail(email)
        val passwordValidationResult = InputValidator.validatePassword(password)

        every { viewModel.onSignInEmailChanged(email) } answers {
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

        every { viewModel.onSignInPasswordChanged(password) } answers {
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

        every { viewModel.signIn(any()) } answers {
            uiState.update { it.copy(isLoading = true) }
            uiState.update { it.copy(isLoading = false) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)
        onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.signIn(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInFrom_disablesSignInButtonWhenEmailIsInvalid() = runComposeUiTest {
        // Given
        val password = "P@ssw0rd"
        uiState.update { it.copy(signInPassword = password, signInPasswordError = null) }

        val email = "test@example"
        val emailValidationResult = InputValidator.validateEmail(email)

        every { viewModel.onSignInEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_disablesSignInButtonWhenPasswordIsInvalid() = runComposeUiTest {
        // Given
        val email = "test@example.com"
        uiState.update { it.copy(signInEmail = email, signInEmailError = null) }

        val password = "password"
        val passwordValidationResult = InputValidator.validatePassword(password)

        every { viewModel.onSignInPasswordChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_disableButtonsWhenUIStateIsLoading() = runComposeUiTest {
        // Given
        uiState.update { it.copy(isLoading = true) }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }

        // Then
        onNodeWithTag(TestTags.SIGN_IN_BUTTON.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_navigatesToForgotPasswordForm() = runComposeUiTest {
        // Given
        every { viewModel.displayAuthForm(AuthForm.ResetPassword) } answers {
            uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.ResetPassword) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signInForm_navigatesToSignUpForm() = runComposeUiTest {
        // Given
        every { viewModel.displayAuthForm(AuthForm.SignUp) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignUp) }
        onNodeWithTag(TestTags.SIGN_UP_FORM.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_displaysCorrectly() = runComposeUiTest {
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        onNodeWithTag(TestTags.SIGN_UP_SCREEN_TITLE.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_updateEmailField() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example.com"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignUpEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onSignUpEmailChanged(email) }
        onNodeWithText(email).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_displaysErrorWhenEmailIsInvalid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val email = "test@example"
        val validationResult = InputValidator.validateEmail(email)
        every { viewModel.onSignUpEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onSignUpEmailChanged(email) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_updatePasswordField() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignUpPasswordChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordChanged(password) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_displaysErrorWhenPasswordIsInvalid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val validationResult = InputValidator.validatePassword(password)
        every { viewModel.onSignUpPasswordChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordChanged(password) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_updateConfirmPasswordField() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "P@ssw0rd"
        val validationResult = InputValidator.validatePasswordConfirmation(password, password)
        every { viewModel.onSignUpPasswordConfirmationChanged(password) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        verify { viewModel.onSignUpPasswordConfirmationChanged(password) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_displaysErrorWhenConfirmPasswordIsInvalid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        val password = "password"
        val confirmPassword = "password123"
        val validationResult = InputValidator.validatePasswordConfirmation(password, confirmPassword)
        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)

        // Then
        verify { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_displayVerifyEmailFormWhenEmailAndPasswordAreValid() = runComposeUiTest {
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
                when (emailValidationResult) {
                    is ValidationResult.Valid -> it.copy(signUpEmail = email, signUpEmailError = null)
                    is ValidationResult.Invalid -> it.copy(
                        signUpEmail = email,
                        signUpEmailError = emailValidationResult.message
                    )
                }
            }
        }

        every { viewModel.onSignUpPasswordChanged(password) } answers {
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

        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
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

        every { viewModel.signUp(any()) } answers {
            uiState.update { it.copy(isLoading = true) }
            uiState.update { it.copy(currentForm = AuthForm.VerifyEmail) }
            uiState.update { it.copy(isLoading = false) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)
        onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)
        onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.signUp(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_disablesSignUpButtonWhenEmailIsInvalid() = runComposeUiTest {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_disablesSignUpButtonWhenPasswordIsInvalid() = runComposeUiTest {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag).performTextInput(password)

        // Then
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_disablesSignUpButtonWhenConfirmPasswordIsInvalid() = runComposeUiTest {
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
        val confirmPasswordValidationResult = InputValidator.validatePasswordConfirmation(password, confirmPassword)

        every { viewModel.onSignUpPasswordConfirmationChanged(confirmPassword) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag).performTextInput(confirmPassword)

        // Then
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_disableButtonsWhenUIStateIsLoading() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp, isLoading = true) }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }

        // Then
        onNodeWithTag(TestTags.SIGN_UP_BUTTON.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun signUpForm_navigatesToSignInForm() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        every { viewModel.displayAuthForm(AuthForm.SignIn) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignIn) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignIn) }
        onNodeWithTag(TestTags.SIGN_IN_FORM.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun verifyEmailForm_displaysCorrectly() = runComposeUiTest {
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, signUpEmail = "test@example.com") }
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_TITLE.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_DESCRIPTION.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EMAIL.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EDIT_EMAIL_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_VERIFY_EMAIL_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.VERIFY_EMAIL_RESEND_EMAIL_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun verifyEmailForm_navigatesToSignUpForm() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail) }
        every { viewModel.displayAuthForm(AuthForm.SignUp) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignUp) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_EDIT_EMAIL_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignUp) }
        verify { viewModel.deleteUser(any()) }
        onNodeWithTag(TestTags.SIGN_UP_FORM.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun verifyEmailForm_disableButtonsWhenUIStateIsLoading() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.VerifyEmail, isLoading = true) }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }

        // Then
        onNodeWithTag(TestTags.VERIFY_EMAIL_SCREEN_VERIFY_EMAIL_BUTTON.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.VERIFY_EMAIL_RESEND_EMAIL_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_displaysCorrectly() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }

        // Then
        onNodeWithTag(TestTags.RESET_PASSWORD_SCREEN_TITLE.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsDisplayed()
        onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_updateEmailField() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example.com"
        val validationResult = InputValidator.validateEmail(email)

        every { viewModel.onPasswordResetEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onPasswordResetEmailChanged(email) }
        onNodeWithText(email).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_displaysErrorWhenEmailIsInvalid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example"
        val validationResult = InputValidator.validateEmail(email)

        every { viewModel.onPasswordResetEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        verify { viewModel.onPasswordResetEmailChanged(email) }
        onNodeWithText((validationResult as ValidationResult.Invalid).message).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_disableButtonsWhenUIStateIsLoading() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword, isLoading = true) }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }

        // Then
        onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsNotEnabled()
        onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_navigatesToSignInForm() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        every { viewModel.displayAuthForm(AuthForm.SignIn) } answers {
            uiState.update { it.copy(currentForm = AuthForm.SignIn) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.displayAuthForm(AuthForm.SignIn) }
        onNodeWithTag(TestTags.SIGN_IN_FORM.tag).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_callsSendPasswordResetEmailWhenEmailIsValid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example.com"
        val emailValidationResult = InputValidator.validateEmail(email)

        every { viewModel.onPasswordResetEmailChanged(email) } answers {
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

        every { viewModel.sendPasswordResetEmail(any()) } answers {
            uiState.update { it.copy(isLoading = true) }
            uiState.update { it.copy(isLoading = false) }
        }

        // When
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)
        onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).performClick()
        waitForIdle()

        // Then
        verify { viewModel.sendPasswordResetEmail(any()) }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun passwordResetForm_disablesResetButtonWhenEmailIsInvalid() = runComposeUiTest {
        // Given
        uiState.update { it.copy(currentForm = AuthForm.ResetPassword) }
        val email = "test@example"
        val emailValidationResult = InputValidator.validateEmail(email)

        every { viewModel.onPasswordResetEmailChanged(email) } answers {
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
        setContent {
            AuthScreen(
                windowSizeClass = windowSizeClass, navController = navController, viewModel = viewModel
            )
        }
        onNodeWithTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag).performTextInput(email)

        // Then
        onNodeWithTag(TestTags.RESET_PASSWORD_BUTTON.tag).assertIsNotEnabled()
    }
}
