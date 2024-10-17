package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.shared.domain.use_case.SignInUseCase
import org.example.shared.domain.use_case.SignUpUseCase
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.AuthUIState
import org.example.shared.util.AuthForm
import org.example.shared.util.validation.InputValidator
import org.example.shared.util.validation.ValidationResult

/**
 * ViewModel class for handling authentication-related operations.
 *
 * @property signUpUseCase The use case for signing up a user.
 * @property signInUseCase The use case for signing in a user.
 * @property dispatcher The coroutine dispatcher used for asynchronous operations.
 */
class AuthViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val dispatcher: CoroutineDispatcher,
) : BaseViewModel(dispatcher)
{

    // StateFlow to hold the current UI state.
    private val _state = MutableStateFlow(AuthUIState())
    val state = _state.asStateFlow()

    /**
     * Updates the sign-in email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    fun onSignInEmailChanged(email: String) = with(InputValidator.validateEmail(email)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signInEmail = email, signInEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signInEmail = email, signInEmailError = message) }
        }
    }

    /**
     * Updates the sign-in password and its validation error state.
     *
     * @param password The new password to validate and update.
     */
    fun onSignInPasswordChanged(password: String) = with(InputValidator.validatePassword(password)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signInPassword = password, signInPasswordError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signInPassword = password, signInPasswordError = message) }
        }
    }

    /**
     * Toggles the visibility of the sign-in password.
     */
    fun toggleSignInPasswordVisibility() = _state.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }

    /**
     * Initiates the sign-in process.
     */
    fun signIn() = with(_state) {
        update { it.copy(isLoading = true) }

        onSignInEmailChanged(value.signInEmail)
        onSignInPasswordChanged(value.signInPassword)

        if (value.signInEmailError.isNullOrBlank() && value.signInPasswordError.isNullOrBlank()) viewModelScope.launch(dispatcher) {
            signInUseCase(value.signInEmail, value.signInPassword)
                .onSuccess { navigate(Route.Dashboard, true) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Updates the sign-up email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    fun onSignUpEmailChanged(email: String) = with(InputValidator.validateEmail(email)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = message) }
        }
    }

    /**
     * Updates the sign-up password and its validation error state.
     *
     * @param password The new password to validate and update.
     */
    fun onSignUpPasswordChanged(password: String) = with(InputValidator.validatePassword(password)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signUpPassword = password, signUpPasswordError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signUpPassword = password, signUpPasswordError = message) }
        }
    }

    /**
     * Toggles the visibility of the sign-up password.
     */
    fun toggleSignUpPasswordVisibility() = _state.update { it.copy(signUpPasswordVisibility = !it.signUpPasswordVisibility) }

    /**
     * Updates the sign-up password confirmation and its validation error state.
     *
     * @param password The new password confirmation to validate and update.
     */
    fun onSignUpPasswordConfirmationChanged(password: String) =
        with(InputValidator.validatePasswordConfirmation(_state.value.signUpPassword, password)) {
            when (this@with)
            {
                is ValidationResult.Valid ->
                    _state.update { it.copy(signUpPasswordConfirmation = password, signUpPasswordConfirmationError = null) }

                is ValidationResult.Invalid ->
                    _state.update { it.copy(signUpPasswordConfirmation = password, signUpPasswordConfirmationError = message) }
            }
        }

    /**
     * Initiates the sign-up process.
     */
    fun signUp() = with(_state) {
        update { it.copy(isLoading = true) }

        onSignUpEmailChanged(value.signUpEmail)
        onSignUpPasswordChanged(value.signUpPassword)
        onSignUpPasswordConfirmationChanged(value.signUpPasswordConfirmation)

        if (
            value.signUpEmailError.isNullOrBlank() &&
            value.signUpPasswordError.isNullOrBlank() &&
            value.signUpPasswordConfirmationError.isNullOrBlank()
        ) viewModelScope.launch(dispatcher) {
            signUpUseCase(value.signUpEmail, value.signUpPassword)
                .onSuccess { navigate(Route.EmailVerification, true) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Displays the specified authentication form.
     *
     * @param form The authentication form to display.
     */
    fun displayAuthForm(form: AuthForm) = when (form)
    {
        AuthForm.SignIn -> _state.update {
            it.copy(
                currentForm = AuthForm.SignIn,
                signInEmail = "",
                signInPassword = "",
                signInEmailError = null,
                signInPasswordError = null
            )
        }

        AuthForm.SignUp -> _state.update {
            it.copy(
                currentForm = AuthForm.SignUp,
                signUpEmail = "",
                signUpPassword = "",
                signUpPasswordConfirmation = "",
                signUpEmailError = null,
                signUpPasswordError = null,
                signUpPasswordConfirmationError = null
            )
        }

        AuthForm.ForgotPassword -> _state.update { it.copy(currentForm = AuthForm.ForgotPassword) }
    }
}
