package org.example.shared.domain.viewModel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.example.shared.domain.use_case.SignInUseCase
import org.example.shared.domain.use_case.SignUpUseCase
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.UIEvent
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.util.validation.InputValidator
import org.example.shared.util.validation.ValidationResult
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class AuthViewModelTest
{
    private lateinit var viewModel: AuthViewModel
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var signInUseCase: SignInUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup()
    {
        Dispatchers.setMain(testDispatcher)
        signUpUseCase = mockk(relaxed = true)
        signInUseCase = mockk(relaxed = true)
        viewModel = AuthViewModel(signUpUseCase, signInUseCase, testDispatcher)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `onSignInEmailChanged with valid email should update email state and set email error to null`()
    {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onSignInEmailChanged(email)

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertNull(viewModel.state.value.signInEmailError)
    }

    @Test
    fun `onSignInEmailChanged with invalid email should update email state and set email error to message`()
    {
        // Given
        val email = "test"
        val message = (InputValidator.validateEmail(email) as ValidationResult.Invalid).message
        // When
        viewModel.onSignInEmailChanged(email)

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertEquals(message, viewModel.state.value.signInEmailError)
    }

    @Test
    fun `onSignInPasswordChanged with valid password should update password state and set password error to null`()
    {
        // Given
        val password = "P@ssw0rd"

        // When
        viewModel.onSignInPasswordChanged(password)

        // Then
        assertEquals(password, viewModel.state.value.signInPassword)
        assertNull(viewModel.state.value.signInPasswordError)
    }

    @Test
    fun `onSignInPasswordChanged with invalid password should update password state and set password error to message`()
    {
        // Given
        val password = "password"
        val message = (InputValidator.validatePassword(password) as ValidationResult.Invalid).message

        // When
        viewModel.onSignInPasswordChanged(password)

        // Then
        assertEquals(password, viewModel.state.value.signInPassword)
        assertEquals(message, viewModel.state.value.signInPasswordError)
    }

    @Test
    fun `toggleSignInPasswordVisibility should toggle signInPasswordVisibility state`()
    {
        // Given
        val initialVisibility = viewModel.state.value.signInPasswordVisibility

        // When
        viewModel.toggleSignInPasswordVisibility()

        // Then
        assertNotEquals(initialVisibility, viewModel.state.value.signInPasswordVisibility)
    }

    @Test
    fun `signIn with valid email and password should call signInUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"

        viewModel.onSignInEmailChanged(email)
        viewModel.onSignInPasswordChanged(password)

        coEvery { signInUseCase(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.signIn()
        advanceUntilIdle()

        // Then
        coVerify { signInUseCase(email, password) }
    }

    @Test
    fun `signIn with invalid email should not call signInUseCase`() = runTest {
        // Given
        val email = "test"
        val password = "P@ssw0rd"

        viewModel.onSignInEmailChanged(email)
        viewModel.onSignInPasswordChanged(password)

        // When
        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with invalid password should not call signInUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"

        viewModel.onSignInEmailChanged(email)
        viewModel.onSignInPasswordChanged(password)

        // When
        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with no prior call to signInEmailChanged should not call signInUseCase`() = runTest {
        // Given
        val password = "P@ssw0rd"

        viewModel.onSignInPasswordChanged(password)

        // When
        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn with no prior call to signInPasswordChanged should not call signInUseCase`() = runTest {
        // Given
        val email = "test@example.com"

        viewModel.onSignInEmailChanged(email)

        // When
        viewModel.signIn()
        testScheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { signInUseCase(any(), any()) }
    }

    @Test
    fun `signIn navigate to Dashboard when signInUseCase returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"

        coEvery { signInUseCase(any(), any()) } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.onSignInEmailChanged(email)
        viewModel.onSignInPasswordChanged(password)
        viewModel.signIn()
        advanceUntilIdle()

        // Simulate the exit animation finishing
        viewModel.onExitAnimationFinished()
        advanceUntilIdle()

        // Then
        assertEquals(email, viewModel.state.value.signInEmail)
        assertNull(viewModel.state.value.signInEmailError)
        assertEquals(password, viewModel.state.value.signInPassword)
        assertNull(viewModel.state.value.signInPasswordError)
        assertEquals(1, uiEvents.size)

        val event = uiEvents.first()

        assertTrue(event is UIEvent.Navigate)
        assertEquals(Route.Dashboard, event.destination)

        job.cancel()
    }

    @Test
    fun `signIn show error message when signInUseCase returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val exception = Exception("An error occurred")

        coEvery { signInUseCase(any(), any()) } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.onSignInEmailChanged(email)
        viewModel.onSignInPasswordChanged(password)
        viewModel.signIn()
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
    fun `onSignUpEmailChanged with valid email should update email state and set email error to null`()
    {
        // Given
        val email = "test@example.com"

        // When
        viewModel.onSignUpEmailChanged(email)

        // Then
        assertEquals(email, viewModel.state.value.signUpEmail)
    }

    @Test
    fun `onSignUpEmailChanged with invalid email should update email state and set email error to message`()
    {
        // Given
        val email = "test"
        val message = (InputValidator.validateEmail(email) as ValidationResult.Invalid).message

        // When
        viewModel.onSignUpEmailChanged(email)

        // Then
        assertEquals(email, viewModel.state.value.signUpEmail)
        assertEquals(message, viewModel.state.value.signUpEmailError)
    }

    @Test
    fun `onSignUpPasswordChanged with valid password should update password state and set password error to null`()
    {
        // Given
        val password = "P@ssw0rd"

        // When
        viewModel.onSignUpPasswordChanged(password)

        // Then
        assertEquals(password, viewModel.state.value.signUpPassword)
        assertNull(viewModel.state.value.signUpPasswordError)
    }

    @Test
    fun `onSignUpPasswordChanged with invalid password should update password state and set password error to message`()
    {
        // Given
        val password = "password"
        val message = (InputValidator.validatePassword(password) as ValidationResult.Invalid).message

        // When
        viewModel.onSignUpPasswordChanged(password)

        // Then
        assertEquals(password, viewModel.state.value.signUpPassword)
        assertEquals(message, viewModel.state.value.signUpPasswordError)
    }

    @Test
    fun `toggleSignUpPasswordVisibility should toggle signUpPasswordVisibility state`()
    {
        // Given
        val initialVisibility = viewModel.state.value.signUpPasswordVisibility

        // When
        viewModel.toggleSignUpPasswordVisibility()

        // Then
        assertNotEquals(initialVisibility, viewModel.state.value.signUpPasswordVisibility)
    }

    @Test
    fun `onSignUpPasswordConfirmationChanged with valid password confirmation should update password confirmation state and set password confirmation error to null`()
    {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"

        // When
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // Then
        assertEquals(passwordConfirmation, viewModel.state.value.signUpPasswordConfirmation)
        assertNull(viewModel.state.value.signUpPasswordConfirmationError)
    }

    @Test
    fun `onSignUpPasswordConfirmationChanged with invalid password confirmation should update password confirmation state and set password confirmation error to message`()
    {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "password"
        val message = (InputValidator.validatePasswordConfirmation(password, passwordConfirmation) as ValidationResult.Invalid).message

        // When
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

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
        coEvery { signUpUseCase(any(), any()) } returns Result.success(Unit)

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()
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

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with invalid password should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val passwordConfirmation = "password"

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with invalid password confirmation should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "password"

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to onSignUpEmailChanged should not call signUpUseCase`() = runTest {
        // Given
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"

        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to onSignUpPasswordChanged should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val passwordConfirmation = "P@ssw0rd"

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp with no prior call to onSignUpPasswordConfirmationChanged should not call signUpUseCase`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"

        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)

        // When
        viewModel.signUp()

        // Then
        coVerify(exactly = 0) { signUpUseCase(any(), any()) }
    }

    @Test
    fun `signUp navigate to EmailVerification when signUpUseCase returns success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"

        coEvery { signUpUseCase(any(), any()) } returns Result.success(Unit)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)
        viewModel.signUp()
        advanceUntilIdle()

        // Simulate the exit animation finishing
        viewModel.onExitAnimationFinished()
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

        assertTrue(event is UIEvent.Navigate)
        assertEquals(Route.EmailVerification, event.destination)

        job.cancel()
    }

    @Test
    fun `signUp show error message when signUpUseCase returns failure`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "P@ssw0rd"
        val passwordConfirmation = "P@ssw0rd"
        val exception = Exception("An error occurred")

        coEvery { signUpUseCase(any(), any()) } returns Result.failure(exception)

        val uiEvents = mutableListOf<UIEvent>()
        val job = launch {
            viewModel.uiEvent.toList(uiEvents)
        }

        // When
        viewModel.onSignUpEmailChanged(email)
        viewModel.onSignUpPasswordChanged(password)
        viewModel.onSignUpPasswordConfirmationChanged(passwordConfirmation)
        viewModel.signUp()
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
}