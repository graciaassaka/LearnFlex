package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.shared.domain.use_case.SignInUseCase
import org.example.shared.domain.use_case.SignUpUseCase
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.AuthUIState
import org.example.shared.presentation.util.AuthForm
import org.example.shared.util.validation.InputValidator
import org.example.shared.util.validation.ValidationResult

class AuthViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val dispatcher: CoroutineDispatcher,
) : BaseViewModel()
{
    private val _state = MutableStateFlow(AuthUIState())
    val state = _state.asStateFlow()

    fun onSignInEmailChanged(email: String) = with(InputValidator.validateEmail(email)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signInEmail = email, signInEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signInEmail = email, signInEmailError = message) }
        }
    }

    fun onSignInPasswordChanged(password: String) = with(InputValidator.validatePassword(password)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signInPassword = password, signInPasswordError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signInPassword = password, signInPasswordError = message) }
        }
    }

    fun toggleSignInPasswordVisibility() = _state.update { it.copy(signInPasswordVisibility = !it.signInPasswordVisibility) }

    fun signIn() = with(_state) {
        update { it.copy(isLoading = true) }

        onSignInEmailChanged(value.signInEmail)
        onSignInPasswordChanged(value.signInPassword)

        if (value.signInEmailError.isNullOrBlank() && value.signInPasswordError.isNullOrBlank()) viewModelScope.launch {
            withContext(dispatcher) { signInUseCase(value.signInEmail, value.signInPassword) }
                .onSuccess { navigate(Route.Dashboard, true) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

    fun onSignUpEmailChanged(email: String) = with(InputValidator.validateEmail(email)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signUpEmail = email, signUpEmailError = message) }
        }
    }

    fun onSignUpPasswordChanged(password: String) = with(InputValidator.validatePassword(password)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(signUpPassword = password, signUpPasswordError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(signUpPassword = password, signUpPasswordError = message) }
        }
    }

    fun toggleSignUpPasswordVisibility() = _state.update { it.copy(signUpPasswordVisibility = !it.signUpPasswordVisibility) }

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

    fun signUp() = with(_state) {
        update { it.copy(isLoading = true) }

        onSignUpEmailChanged(value.signUpEmail)
        onSignUpPasswordChanged(value.signUpPassword)
        onSignUpPasswordConfirmationChanged(value.signUpPasswordConfirmation)

        if (
            value.signUpEmailError.isNullOrBlank() &&
            value.signUpPasswordError.isNullOrBlank() &&
            value.signUpPasswordConfirmationError.isNullOrBlank()
        ) viewModelScope.launch {
            withContext(dispatcher) { signUpUseCase(value.signUpEmail, value.signUpPassword) }
                .onSuccess { navigate(Route.EmailVerification, true) }
                .onFailure { error -> handleError(error) }
        }

        update { it.copy(isLoading = false) }
    }

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