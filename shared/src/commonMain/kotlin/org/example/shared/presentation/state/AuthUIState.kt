package org.example.shared.presentation.state

import org.example.shared.util.AuthForm

/**
 * Data class representing the UI state for authentication.
 *
 * @property currentForm The current form being displayed (SignIn or SignUp).
 * @property signInEmail The email entered in the sign-in form.
 * @property signInEmailError The error message for the sign-in email, if any.
 * @property signInPassword The password entered in the sign-in form.
 * @property signInPasswordError The error message for the sign-in password, if any.
 * @property signInPasswordVisibility Whether the sign-in password is visible or not.
 * @property signUpEmail The email entered in the sign-up form.
 * @property signUpEmailError The error message for the sign-up email, if any.
 * @property signUpPassword The password entered in the sign-up form.
 * @property signUpPasswordError The error message for the sign-up password, if any.
 * @property signUpPasswordVisibility Whether the sign-up password is visible or not.
 * @property signUpPasswordConfirmationError The error message for the sign-up password confirmation, if any.
 * @property signUpPasswordConfirmation The password confirmation entered in the sign-up form.
 * @property isLoading Whether a loading indicator should be displayed or not.
 */
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