package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.AuthAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.AuthUIState
import org.example.composeApp.presentation.ui.screen.AuthForm
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.shared.domain.use_case.auth.*
import org.example.shared.domain.use_case.validation.ValidateEmailUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordConfirmationUseCase
import org.example.shared.domain.use_case.validation.ValidatePasswordUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.jetbrains.compose.resources.getString

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
     * Handles the specified action.
     *
     * @param action The action to handle.
     */
    fun handleAction(action: AuthAction) {
        when (action) {
            is AuthAction.EditSignInEmail -> editSignInEmail(action.email)
            is AuthAction.EditSignInPassword -> editSignInPassword(action.password)
            is AuthAction.ToggleSignInPasswordVisibility -> toggleSignInPasswordVisibility()
            is AuthAction.SignIn -> signIn()
            is AuthAction.EditSignUpEmail -> editSignUpEmail(action.email)
            is AuthAction.EditSignUpPassword -> editSignUpPassword(action.password)
            is AuthAction.ToggleSignUpPasswordVisibility -> toggleSignUpPasswordVisibility()
            is AuthAction.EditSignUpPasswordConfirmation -> editSignUpPasswordConfirmation(action.password)
            is AuthAction.SignUp -> signUp()
            is AuthAction.ResendVerificationEmail -> resendVerificationEmail()
            is AuthAction.VerifyEmail -> verifyEmail()
            is AuthAction.DeleteUser -> deleteUser()
            is AuthAction.EditPasswordResetEmail -> editPasswordResetEmail(action.email)
            is AuthAction.SendPasswordResetEmail -> sendPasswordResetEmail()
            is AuthAction.DisplayAuthForm -> displayAuthForm(action.form)
            is AuthAction.HandleAnimationEnd -> handleExitAnimationFinished()
        }
    }

    /**
     * Updates the sign-in email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    private fun editSignInEmail(email: String) = with(validateEmailUseCase(email)) {
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
    private fun editSignInPassword(password: String) = with(validatePasswordUseCase(password)) {
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
    private fun toggleSignInPasswordVisibility() =
        _state.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }

    /**
     * Initiates the sign-in process.
     */
    private fun signIn() = with(_state) {
        update { it.copy(isLoading = true) }

        editSignInEmail(value.signInEmail)
        editSignInPassword(value.signInPassword)

        if (value.signInEmailError.isNullOrBlank() && value.signInPasswordError.isNullOrBlank())
            viewModelScope.launch(dispatcher) {
                val successMessage = async { getString(Res.string.sign_in_success) }
                signInUseCase(
                    email = value.signInEmail,
                    password = value.signInPassword
                ).onSuccess {
                    update { it.copy(isUserSignedIn = true) }
                    navigate(Route.Dashboard, true)
                    showSnackbar(successMessage.await(), SnackbarType.Success)
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
    private fun editSignUpEmail(email: String) = with(validateEmailUseCase(email)) {
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
    private fun editSignUpPassword(password: String) = with(validatePasswordUseCase(password)) {
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
    private fun toggleSignUpPasswordVisibility() =
        _state.update { it.copy(signUpPasswordVisibility = !it.signUpPasswordVisibility) }

    /**
     * Updates the sign-up password confirmation and its validation error state.
     *
     * @param password The new password confirmation to validate and update.
     */
    private fun editSignUpPasswordConfirmation(password: String) =
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
    private fun signUp() = with(_state) {
        update { it.copy(isLoading = true) }

        editSignUpEmail(value.signUpEmail)
        editSignUpPassword(value.signUpPassword)
        editSignUpPasswordConfirmation(value.signUpPasswordConfirmation)

        if (
            value.signUpEmailError.isNullOrBlank() &&
            value.signUpPasswordError.isNullOrBlank() &&
            value.signUpPasswordConfirmationError.isNullOrBlank()
        ) viewModelScope.launch(dispatcher) {
            val successMessage = async { getString(Res.string.sign_up_success) }
            signUpUseCase(
                email = value.signUpEmail,
                password = value.signUpPassword
            ).onSuccess {
                update { it.copy(isUserSignedUp = true) }
                showSnackbar(successMessage.await(), SnackbarType.Success)
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
    private fun resendVerificationEmail() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            val successMessage = async { getString(Res.string.resend_email_success) }
            sendVerificationEmailUseCase()
                .onSuccess { showSnackbar(successMessage.await(), SnackbarType.Success) }
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
    private fun verifyEmail() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            verifyEmailUseCase().onSuccess {
                update { it.copy(isEmailVerified = true) }
                navigate(Route.CreateProfile, true)
            }.onFailure { error ->
                handleError(error)
            }
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
    private fun deleteUser() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            val successMessage = async { getString(Res.string.del_user_success) }
            deleteUserUseCase()
                .onSuccess { showSnackbar(successMessage.await(), SnackbarType.Success) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Updates the reset password email and its validation error state.
     *
     * @param email The new email to validate and update.
     */
    private fun editPasswordResetEmail(email: String) = with(validateEmailUseCase(email)) {
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
     */
    private fun sendPasswordResetEmail() = with(_state) {
        update { it.copy(isLoading = true) }

        editPasswordResetEmail(value.resetPasswordEmail)

        if (value.resetPasswordEmailError.isNullOrBlank()) viewModelScope.launch(dispatcher) {
            val successMessage = async { getString(Res.string.resend_email_success) }
            sendPasswordResetEmailUseCase(value.resetPasswordEmail).onSuccess {
                update { it.copy(isPasswordResetEmailSent = true) }
                showSnackbar(successMessage.await(), SnackbarType.Success)
            }.onFailure { error ->
                handleError(error)
            }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Displays the specified authentication form.
     *
     * @param form The authentication form to display.
     */
    private fun displayAuthForm(form: AuthForm) = when (form) {
        AuthForm.SignIn -> _state.update {
            AuthUIState().copy(currentForm = AuthForm.SignIn)
        }

        AuthForm.SignUp -> _state.update {
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