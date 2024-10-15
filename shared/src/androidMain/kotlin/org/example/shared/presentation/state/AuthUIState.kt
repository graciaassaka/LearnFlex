package org.example.shared.presentation.state

import org.example.shared.presentation.util.AuthForm

data class AuthUIState(
    val currentForm: AuthForm = AuthForm.SignIn,
    val signInEmail: String = "",
    val signInEmailError: String? = null,
    val signInPassword: String = "",
    val signInPasswordError: String? = null,
    val signInPasswordVisibility: Boolean = false,
    val signUpEmail: String = "",
    val signUpEmailError: String? = null,
    val signUpPassword: String = "",
    val signUpPasswordError: String? = null,
    val signUpPasswordVisibility: Boolean = false,
    val signUpPasswordConfirmationError: String? = null,
    val signUpPasswordConfirmation: String = "",
    val isLoading: Boolean = false
)
