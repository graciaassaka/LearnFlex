package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.shared.domain.use_case.auth.*
import org.example.shared.domain.use_case.validation.ValidateEmailUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordConfirmationUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.AuthUIState
import org.example.shared.presentation.util.AuthForm
import org.example.shared.presentation.util.SnackbarType

/**
 * ViewModel class for handling authentication-related operations.
 *
 * @property signUpUseCase The use case for signing up a user.
 * @property signInUseCase The use case for signing in a user.
 * @property sendVerificationEmailUseCase The use case for sending a verification email.
 * @property verifyEmailUseCase The use case for verifying a user's email.
 * @property deleteUserUseCase The use case for deleting a user.
 * @property sendPasswordResetEmailUseCase The use case for sending a password reset email.
 * @property validateEmailUseCase The use case for validating an email.
 * @property validatePasswordUseCase The use case for validating a password.
 * @property validatePasswordConfirmationUseCase The use case for validating a password confirmation.
 * @property dispatcher The coroutine dispatcher used for asynchronous operations.
 */
class AuthViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validatePasswordConfirmationUseCase: ValidatePasswordConfirmationUseCase,
    private val dispatcher: CoroutineDispatcher,
) : BaseViewModel(dispatcher) {

    // StateFlow to hold the current UI state.
    private val _state = MutableStateFlow(AuthUIState())
    val state = _state.asStateFlow()

    /**
     * Updates the sign-in email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    fun onSignInEmailChanged(email: String) = with(validateEmailUseCase(email)) {
        when (this@with) {
            is ValidationResult.Valid -> _state.update { it.copy(signInEmail = email, signInEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signInEmail = email, signInEmailError = message) }
        }
    }

    /**
     * Updates the sign-in password and its validation error state.
     *
     * @param password The new password to validate and update.
     */
    fun onSignInPasswordChanged(password: String) = with(validatePasswordUseCase(password)) {
        when (this@with) {
            is ValidationResult.Valid -> _state.update {
                it.copy(
                    signInPassword = password,
                    signInPasswordError = null
                )
            }

            is ValidationResult.Invalid -> _state.update {
                it.copy(
                    signInPassword = password,
                    signInPasswordError = message
                )
            }
        }
    }

    /**
     * Toggles the visibility of the sign-in password.
     */
    fun toggleSignInPasswordVisibility() =
        _state.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }

    /**
     * Initiates the sign-in process.
     */
    fun signIn(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        onSignInEmailChanged(value.signInEmail)
        onSignInPasswordChanged(value.signInPassword)

        if (value.signInEmailError.isNullOrBlank() && value.signInPasswordError.isNullOrBlank()) viewModelScope.launch(
            dispatcher
        ) {
            signInUseCase(
                email = value.signInEmail,
                password = value.signInPassword
            ).onSuccess {
                navigate(Route.Dashboard, true)
                showSnackbar(successMessage, SnackbarType.Success)
            }.onFailure { error ->
                handleError(error)
            }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Updates the sign-up email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    fun onSignUpEmailChanged(email: String) = with(validateEmailUseCase(email)) {
        when (this@with) {
            is ValidationResult.Valid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = message) }
        }
    }

    /**
     * Updates the sign-up password and its validation error state.
     *
     * @param password The new password to validate and update.
     */
    fun onSignUpPasswordChanged(password: String) = with(validatePasswordUseCase(password)) {
        when (this@with) {
            is ValidationResult.Valid -> _state.update {
                it.copy(
                    signUpPassword = password,
                    signUpPasswordError = null
                )
            }

            is ValidationResult.Invalid -> _state.update {
                it.copy(
                    signUpPassword = password,
                    signUpPasswordError = message
                )
            }
        }
    }

    /**
     * Toggles the visibility of the sign-up password.
     */
    fun toggleSignUpPasswordVisibility() =
        _state.update { it.copy(signUpPasswordVisibility = !it.signUpPasswordVisibility) }

    /**
     * Updates the sign-up password confirmation and its validation error state.
     *
     * @param password The new password confirmation to validate and update.
     */
    fun onSignUpPasswordConfirmationChanged(password: String) =
        with(validatePasswordConfirmationUseCase(_state.value.signUpPassword, password)) {
            when (this@with) {
                is ValidationResult.Valid ->
                    _state.update {
                        it.copy(
                            signUpPasswordConfirmation = password,
                            signUpPasswordConfirmationError = null
                        )
                    }

                is ValidationResult.Invalid ->
                    _state.update {
                        it.copy(
                            signUpPasswordConfirmation = password,
                            signUpPasswordConfirmationError = message
                        )
                    }
            }
        }

    /**
     * Initiates the sign-up process.
     */
    fun signUp(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        onSignUpEmailChanged(value.signUpEmail)
        onSignUpPasswordChanged(value.signUpPassword)
        onSignUpPasswordConfirmationChanged(value.signUpPasswordConfirmation)

        if (
            value.signUpEmailError.isNullOrBlank() &&
            value.signUpPasswordError.isNullOrBlank() &&
            value.signUpPasswordConfirmationError.isNullOrBlank()
        ) viewModelScope.launch(dispatcher) {
            signUpUseCase(
                email = value.signUpEmail,
                password = value.signUpPassword
            ).onSuccess {
                update { it.copy(isUserSignedUp = true) }
                showSnackbar(successMessage, SnackbarType.Success)
            }.onFailure { error ->
                handleError(error)
            }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Resends the verification email to the user.
     *
     * This function sets the loading state to true, calls the `sendVerificationEmailUseCase`,
     * and handles the success and failure cases by showing a snackbar with appropriate messages.
     * Finally, it sets the loading state back to false.
     */
    fun resendVerificationEmail(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            sendVerificationEmailUseCase()
                .onSuccess { showSnackbar(successMessage, SnackbarType.Success) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Initiates the email verification process and updates UI state accordingly.
     *
     * Sets the loading state to true, then calls the `verifyEmailUseCase` to verify the user's email.
     * On success, navigates to the Dashboard route. On failure, handles the error by showing
     * an appropriate message.
     */
    fun verifyEmail() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            verifyEmailUseCase()
                .onSuccess { navigate(Route.CreateProfile, true) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Initiates the user deletion process and updates UI state accordingly.
     *
     * Sets the loading state to true, then calls the `deleteUserUseCase` to delete the user's account.
     * On success, navigates to the Auth route. On failure, handles the error by showing
     * an appropriate message.
     */
    fun deleteUser(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            deleteUserUseCase()
                .onSuccess { showSnackbar(successMessage, SnackbarType.Success) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Updates the reset password email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    fun onPasswordResetEmailChanged(email: String) = with(validateEmailUseCase(email)) {
        when (this@with) {
            is ValidationResult.Valid -> _state.update {
                it.copy(
                    resetPasswordEmail = email,
                    resetPasswordEmailError = null
                )
            }

            is ValidationResult.Invalid -> _state.update {
                it.copy(
                    resetPasswordEmail = email,
                    resetPasswordEmailError = message
                )
            }
        }
    }

    /**
     * Sends a password reset email to the user.
     *
     * This function sets the loading state to true, validates the reset password email,
     * and if valid, calls the `sendPasswordResetEmailUseCase` to send the email.
     * On success, shows a success snackbar. On failure, handles the error.
     * Finally, it sets the loading state back to false.
     *
     * @param successMessage The message to show on successful email sending.
     */
    fun sendPasswordResetEmail(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        onPasswordResetEmailChanged(value.resetPasswordEmail)

        if (value.resetPasswordEmailError.isNullOrBlank()) viewModelScope.launch(dispatcher) {
            sendPasswordResetEmailUseCase(value.resetPasswordEmail)
                .onSuccess { showSnackbar(successMessage, SnackbarType.Success) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Displays the specified authentication form.
     *
     * @param form The authentication form to display.
     */
    fun displayAuthForm(form: AuthForm) = when (form) {
        AuthForm.SignIn      -> _state.update {
            AuthUIState().copy(currentForm = AuthForm.SignIn)
        }

        AuthForm.SignUp      -> _state.update {
            AuthUIState().copy(currentForm = AuthForm.SignUp)
        }

        AuthForm.VerifyEmail -> _state.update {
            it.copy(currentForm = AuthForm.VerifyEmail)
        }

        AuthForm.ResetPassword -> _state.update {
            AuthUIState().copy(currentForm = AuthForm.ResetPassword)
        }
    }
}
