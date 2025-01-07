package org.example.shared.presentation.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.composeApp.presentation.action.AuthAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.ui.screen.AuthForm
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.AuthViewModel
import org.example.shared.domain.use_case.auth.*
import org.example.shared.domain.use_case.validation.ValidateEmailUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordConfirmationUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class AuthViewModelTest {
    private lateinit var viewModel: AuthViewModel
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var sendVerificationEmailUseCase: SendVerificationEmailUseCase
    private lateinit var verifyEmailUseCase: VerifyEmailUseCase
    private lateinit var deleteUserUseCase: DeleteUserUseCase
    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
    private lateinit var validateEmailUseCase: ValidateEmailUseCase
    private lateinit var validatePasswordUseCase: ValidatePasswordUseCase
    private lateinit var validatePasswordConfirmationUseCase: ValidatePasswordConfirmationUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        signUpUseCase = mockk(relaxed = true)
        signInUseCase = mockk(relaxed = true)
        sendVerificationEmailUseCase = mockk(relaxed = true)
        verifyEmailUseCase = mockk(relaxed = true)
        deleteUserUseCase = mockk(relaxed = true)
        sendPasswordResetEmailUseCase = mockk(relaxed = true)
        validateEmailUseCase = ValidateEmailUseCase()
        validatePasswordUseCase = ValidatePasswordUseCase()
        validatePasswordConfirmationUseCase = ValidatePasswordConfirmationUseCase()

        viewModel = AuthViewModel(
            signUpUseCase = signUpUseCase,
            signInUseCase = signInUseCase,
            sendVerificationEmailUseCase = sendVerificationEmailUseCase,
            verifyEmailUseCase = verifyEmailUseCase,
            deleteUserUseCase = deleteUserUseCase,
            sendPasswordResetEmailUseCase = sendPasswordResetEmailUseCase,
            validateEmailUseCase = validateEmailUseCase,
            validatePasswordUseCase = validatePasswordUseCase,
            validatePasswordConfirmationUseCase = validatePasswordConfirmationUseCase,
            dispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `editSignInEmail with valid email should update email state and set email error to null`() {
        // Given
        val email = "test@example.com"

        // When
        viewModel.handleAction(AuthAction.EditSignInEmail(email))

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertNull(viewModel.state.value.signInEmailError)
    }

    @Test
    fun `editSignInEmail with invalid email should update email state and set email error to message`() {
        // Given
        val email = "test"
        val message = (validateEmailUseCase(email) as ValidationResult.Invalid).message
        // When
        viewModel.handleAction(AuthAction.EditSignInEmail(email))

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertEquals(message, viewModel.state.value.signInEmailError)
    }

    @Test
    fun `editSignInPassword with valid password should update password state and set password error to null`() {
        // Given
        val password = "P@ssw0rd"

        // When
        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        // Then
        assertEquals(password, viewModel.state.value.signInPassword)
        assertNull(viewModel.state.value.signInPasswordError)
    }

    @Test
    fun `editSignInPassword with invalid password should update password state and set password error to message`() {
        // Given
        val password = "password"
        val message = (validatePasswordUseCase(password) as ValidationResult.Invalid).message

        // When
        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        // Then
        assertEquals(password, viewModel.state.value.signInPassword)
        assertEquals(message, viewModel.state.value.signInPasswordError)
    }

    @Test
    fun `toggleSignInPasswordVisibility should toggle signInPasswordVisibility state`() {
        // Given
        val initialVisibility = viewModel.state.value.signInPasswordVisibility

        // When
        viewModel.handleAction(AuthAction.ToggleSignInPasswordVisibility)

        // Then
        assertNotEquals(initialVisibility, viewModel.state.value.signInPasswordVisibility)
    }

    @Test
    fun `signIn with valid email and password should call signInUseCase`() = runTest {
        // Given
        val successMessage = "User signed in successfully"
        val email = "test@example.com"
        val password = "P@ssw0rd"
        viewModel.handleAction(AuthAction.EditSignInEmail(email))
        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        coEvery { signInUseCase(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        advanceUntilIdle()

        // Then
        coVerify { signInUseCase(email, password) }
    }

    @Test
    fun `signIn with invalid email should not call signInUseCase`() = runTest {
        // Given
        val email = "test"
        val password = "P@ssw0rd"
        val successMessage = "User signed in successfully"

        viewModel.handleAction(AuthAction.EditSignInEmail(email))
        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        // When
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with invalid password should not call signInUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val successMessage = "User signed in successfully"

        viewModel.handleAction(AuthAction.EditSignInEmail(email))
        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        // When
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with no prior call to signInEmailChanged should not call signInUseCase`() = runTest {
        // Given
        val password = "P@ssw0rd"
        val successMessage = "User signed in successfully"

        viewModel.handleAction(AuthAction.EditSignInPassword(password))

        // When
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with no prior call to signInPasswordChanged should not call signInUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val successMessage = "User signed in successfully"
        viewModel.handleAction(AuthAction.EditSignInEmail(email))

        // When
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn displays success message and navigate to Dashboard when signInUseCase returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val successMessage = "User signed in successfully"

        coEvery { signInUseCase(any(), any()) } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.EditSignInEmail(email))
        viewModel.handleAction(AuthAction.EditSignInPassword(password))
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        advanceUntilIdle()

        // Simulate the exit animation finishing
        viewModel.handleExitAnimationFinished()
        advanceUntilIdle()

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertNull(viewModel.state.value.signInEmailError)
        assertEquals(password, viewModel.state.value.signInPassword)
        assertNull(viewModel.state.value.signInPasswordError)
        assertEquals(2, uiEvents.size)

        val displayEvent = uiEvents.first()
        val navEvent = uiEvents.last()

        assertTrue(displayEvent is UIEvent.ShowSnackbar)
        assertEquals(successMessage, displayEvent.message)
        assertTrue(navEvent is UIEvent.Navigate)
        assertEquals(Route.Dashboard, navEvent.destination)

        job.cancel()
    }

    @Test
    fun `signIn show error message when signInUseCase returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val exception = Exception("An error occurred")
        val successMessage = "User signed in successfully"

        coEvery { signInUseCase(any(), any()) } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.EditSignInEmail(email))
        viewModel.handleAction(AuthAction.EditSignInPassword(password))
        viewModel.handleAction(AuthAction.SignIn(successMessage))
        advanceUntilIdle()

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertNull(viewModel.state.value.signInEmailError)
        assertEquals(password, viewModel.state.value.signInPassword)
        assertNull(viewModel.state.value.signInPasswordError)
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(exception.message, event.message)

        job.cancel()
    }


    @Test
    fun `editSignUpEmail with valid email should update email state and set email error to null`() {
        // Given
        val email = "test@example.com"

        // When
        viewModel.handleAction(AuthAction.EditSignUpEmail(email))

        // Then
        assertEquals(email, viewModel.state.value.signUpEmail)
    }

    @Test
    fun `editSignUpEmail with invalid email should update email state and set email error to message`() {
        // Given
        val email = "test"
        val message = (validateEmailUseCase(email) as ValidationResult.Invalid).message

        // When
        viewModel.handleAction(AuthAction.EditSignUpEmail(email))

        // Then
        assertEquals(email, viewModel.state.value.signUpEmail)
        assertEquals(message, viewModel.state.value.signUpEmailError)
    }

    @Test
    fun `editSignUpPassword with valid password should update password state and set password error to null`() {
        // Given
        val password = "P@ssw0rd"

        // When
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))

        // Then
        assertEquals(password, viewModel.state.value.signUpPassword)
        assertNull(viewModel.state.value.signUpPasswordError)
    }

    @Test
    fun `editSignUpPassword with invalid password should update password state and set password error to message`() {
        // Given
        val password = "password"
        val message = (validatePasswordUseCase(password) as ValidationResult.Invalid).message

        // When
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))

        // Then
        assertEquals(password, viewModel.state.value.signUpPassword)
        assertEquals(message, viewModel.state.value.signUpPasswordError)
    }

    @Test
    fun `toggleSignUpPasswordVisibility should toggle signUpPasswordVisibility state`() {
        // Given
        val initialVisibility = viewModel.state.value.signUpPasswordVisibility

        // When
        viewModel.handleAction(AuthAction.ToggleSignUpPasswordVisibility)

        // Then
        assertNotEquals(initialVisibility, viewModel.state.value.signUpPasswordVisibility)
    }

    @Test
    fun `onSignUpPasswordConfirmationChanged with valid password confirmation should update password confirmation state and set password confirmation error to null`() {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"

        // When
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // Then
        assertEquals(passwordConfirmation, viewModel.state.value.signUpPasswordConfirmation)
        assertNull(viewModel.state.value.signUpPasswordConfirmationError)
    }

    @Test
    fun `onSignUpPasswordConfirmationChanged with invalid password confirmation should update password confirmation state and set password confirmation error to message`() {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "password"
        val message = (validatePasswordConfirmationUseCase(
            password,
            passwordConfirmation
        ) as ValidationResult.Invalid).message

        // When
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // Then
        assertEquals(passwordConfirmation, viewModel.state.value.signUpPasswordConfirmation)
        assertEquals(message, viewModel.state.value.signUpPasswordConfirmationError)
    }

    @Test
    fun `signUp with valid email, password, and password confirmation should call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"
        coEvery { signUpUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))
        advanceUntilIdle()

        // Then
        coVerify { signUpUseCase(email, password) }
    }

    @Test
    fun `signUp with invalid email should not call signUpUseCase`() = runTest {
        // Given
        val email = "test"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with invalid password should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val passwordConfirmation = "password"
        val successMessage = "User signed up successfully"

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with invalid password confirmation should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "password"
        val successMessage = "User signed"

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to editSignUpEmail should not call signUpUseCase`() = runTest {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"

        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to editSignUpPassword should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to onSignUpPasswordConfirmationChanged should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val successMessage = "User signed up successfully"

        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))

        // When
        viewModel.handleAction(AuthAction.SignUp(successMessage))

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp update state and show success message when signUpUseCase returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"

        coEvery { signUpUseCase(any(), any()) } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))
        viewModel.handleAction(AuthAction.SignUp(successMessage))
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.isUserSignedUp)
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(successMessage, event.message)

        job.cancel()
    }

    @Test
    fun `signUp show error message when signUpUseCase returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val successMessage = "User signed up successfully"
        val exception = Exception("An error occurred")

        coEvery { signUpUseCase(any(), any()) } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.EditSignUpEmail(email))
        viewModel.handleAction(AuthAction.EditSignUpPassword(password))
        viewModel.handleAction(AuthAction.EditSignUpPasswordConfirmation(passwordConfirmation))
        viewModel.handleAction(AuthAction.SignUp(successMessage))
        advanceUntilIdle()

        // Then
        assertEquals(email, viewModel.state.value.signUpEmail)
        assertNull(viewModel.state.value.signUpEmailError)
        assertEquals(password, viewModel.state.value.signUpPassword)
        assertNull(viewModel.state.value.signUpPasswordError)
        assertEquals(passwordConfirmation, viewModel.state.value.signUpPasswordConfirmation)
        assertNull(viewModel.state.value.signUpPasswordConfirmationError)

        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(exception.message, event.message)

        job.cancel()
    }

    @Test
    fun `resendVerificationEmail should call sendVerificationEmailUseCase`() = runTest {
        // Given
        val successMessage = "Verification email sent successfully"
        coEvery { sendVerificationEmailUseCase() } returns Result.success(Unit)

        // When
        viewModel.handleAction(AuthAction.ResendVerificationEmail(successMessage))
        advanceUntilIdle()

        // Then
        coVerify { sendVerificationEmailUseCase() }
    }

    @Test
    fun `resendVerificationEmail displays success message when sendVerificationEmailUseCase returns success`() =
        runTest {
            // Given
            coEvery { sendVerificationEmailUseCase() } returns Result.success(Unit)

            val uiEvents = mutableListOf<UIEvent>()
            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }
            val successMessage = "Verification email sent successfully"

            // When
            viewModel.handleAction(AuthAction.ResendVerificationEmail(successMessage))
            advanceUntilIdle()

            // Then
            assertEquals(1, uiEvents.size)

            val event = uiEvents.first()

            assertTrue(event is UIEvent.ShowSnackbar)
            assertEquals(successMessage, event.message)

            job.cancel()
        }

    @Test
    fun `resendVerificationEmail displays error message when sendVerificationEmailUseCase returns failure`() = runTest {
        // Given
        val exception = Exception("An error occurred")
        coEvery { sendVerificationEmailUseCase() } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }
        val successMessage = "Verification email sent successfully"

        // When
        viewModel.handleAction(AuthAction.ResendVerificationEmail(successMessage))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(exception.message, event.message)

        job.cancel()
    }

    @Test
    fun `verifyEmail should call verifyEmailUseCase`() = runTest {
        // Given
        coEvery { verifyEmailUseCase() } returns Result.success(Unit)

        // When
        viewModel.handleAction(AuthAction.VerifyEmail)
        advanceUntilIdle()

        // Then
        coVerify { verifyEmailUseCase() }
    }

    @Test
    fun `verifyEmail navigate to CreateProfile when verifyEmailUseCase returns success`() = runTest {
        // Given
        coEvery { verifyEmailUseCase() } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.VerifyEmail)
        advanceUntilIdle()

        // Simulate the exit animation finishing
        viewModel.handleExitAnimationFinished()
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.Navigate)
        assertEquals(Route.CreateProfile, event.destination)

        job.cancel()
    }

    @Test
    fun `verifyEmail show error message when verifyEmailUseCase returns failure`() = runTest {
        // Given
        val exception = Exception("An error occurred")
        coEvery { verifyEmailUseCase() } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.VerifyEmail)
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(exception.message, event.message)

        job.cancel()
    }

    @Test
    fun `deleteUser should call deleteUserUseCase`() = runTest {
        // Given
        val successMessage = "User deleted successfully"
        coEvery { deleteUserUseCase() } returns Result.success(Unit)

        // When
        viewModel.handleAction(AuthAction.DeleteUser(successMessage))
        advanceUntilIdle()

        // Then
        coVerify { deleteUserUseCase() }
    }

    @Test
    fun `deleteUser displays success message when deleteUserUseCase returns success`() = runTest {
        // Given
        val successMessage = "User deleted successfully"
        coEvery { deleteUserUseCase() } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.DeleteUser(successMessage))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(successMessage, event.message)

        job.cancel()
    }

    @Test
    fun `deleteUser displays error message when deleteUserUseCase returns failure`() = runTest {
        // Given
        val successMessage = "User deleted successfully"
        val exception = Exception("An error occurred")
        coEvery { deleteUserUseCase() } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.handleAction(AuthAction.DeleteUser(successMessage))
        advanceUntilIdle()

        // Then
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.ShowSnackbar)
        assertEquals(exception.message, event.message)

        job.cancel()
    }

    @Test
    fun `editPasswordResetEmail with valid email should update email state and set email error to null`() =
        runTest {
            // Given
            val email = "test@example.com"

            // When
            viewModel.handleAction(AuthAction.EditPasswordResetEmail(email))

            // Then
            assertEquals(email, viewModel.state.value.resetPasswordEmail)
            assertNull(viewModel.state.value.resetPasswordEmailError)
        }

    @Test
    fun `editPasswordResetEmail with invalid email should update email state and set email error to message`() =
        runTest {
            // Given
            val email = "test"
            val message = (validateEmailUseCase(email) as ValidationResult.Invalid).message

            // When
            viewModel.handleAction(AuthAction.EditPasswordResetEmail(email))

            // Then
            assertEquals(email, viewModel.state.value.resetPasswordEmail)
            assertEquals(message, viewModel.state.value.resetPasswordEmailError)
        }

    @Test
    fun `sendPasswordResetEmail with valid email should call sendPasswordResetEmailUseCase`() = runTest {
        // Given
        val email = "test@gmail.com"
        val successMessage = "Password reset email sent successfully"

        coEvery { sendPasswordResetEmailUseCase(any()) } returns Result.success(Unit)

        // When
        viewModel.handleAction(AuthAction.EditPasswordResetEmail(email))
        viewModel.handleAction(AuthAction.SendPasswordResetEmail(successMessage))
        advanceUntilIdle()

        // Then
        coVerify { sendPasswordResetEmailUseCase(email) }
    }

    @Test
    fun `sendPasswordResetEmail with invalid email should not call sendPasswordResetEmailUseCase`() = runTest {
        // Given
        val email = "test"
        val successMessage = "Password reset email sent successfully"

        // When
        viewModel.handleAction(AuthAction.EditPasswordResetEmail(email))
        viewModel.handleAction(AuthAction.SendPasswordResetEmail(successMessage))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { sendPasswordResetEmailUseCase(any()) }
    }

    @Test
    fun `sendPasswordResetEmail displays success message when sendPasswordResetEmailUseCase returns success`() =
        runTest {
            // Given
            val email = "test@example.com"
            val successMessage = "Password reset email sent successfully"

            coEvery { sendPasswordResetEmailUseCase(email) } returns Result.success(Unit)

            val uiEvents = mutableListOf<UIEvent>()

            val job = launch {
                viewModel.uiEvent.toList(uiEvents)
            }

            // When
            viewModel.handleAction(AuthAction.EditPasswordResetEmail(email))
            viewModel.handleAction(AuthAction.SendPasswordResetEmail(successMessage))
            advanceUntilIdle()

            // Then
            assertEquals(1, uiEvents.size)

            val event = uiEvents.first()

            assertTrue(event is UIEvent.ShowSnackbar)
            assertEquals(successMessage, event.message)

            job.cancel()
        }

    @Test
    fun `displayAuthForm should update currentForm state when form is SignIn`() = runTest {
        // Given
        val form = AuthForm.SignIn

        // When
        viewModel.handleAction(AuthAction.DisplayAuthForm(form))

        // Then
        assertEquals(AuthForm.SignIn, viewModel.state.value.currentForm)
    }

    @Test
    fun `displayAuthForm should update currentForm state when form is SignUp`() = runTest {
        // Given
        val form = AuthForm.SignUp

        // When
        viewModel.handleAction(AuthAction.DisplayAuthForm(form))

        // Then
        assertEquals(AuthForm.SignUp, viewModel.state.value.currentForm)
    }

    @Test
    fun `displayAuthForm should update currentForm state when form is VerifyEmail`() = runTest {
        // Given
        val form = AuthForm.VerifyEmail

        // When
        viewModel.handleAction(AuthAction.DisplayAuthForm(form))

        // Then
        assertEquals(AuthForm.VerifyEmail, viewModel.state.value.currentForm)
    }

    @Test
    fun `displayAuthForm should update currentForm state when form is ResetPassword`() = runTest {
        // Given
        val form = AuthForm.ResetPassword

        // When
        viewModel.handleAction(AuthAction.DisplayAuthForm(form))

        // Then
        assertEquals(AuthForm.ResetPassword, viewModel.state.value.currentForm)
    }
}