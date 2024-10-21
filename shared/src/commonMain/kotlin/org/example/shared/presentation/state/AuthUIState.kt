package org.example.shared.presentation.state

import org.example.shared.presentation.util.AuthForm

/**
 * Data class representing the state of the authentication UI.
 *
 * @property currentForm The current authentication form being displayed.
 * @property signInEmail The email entered for sign-in.
 * @property signInEmailError The error message related to the sign-in email.
 * @property signInPassword The password entered for sign-in.
 * @property signInPasswordError The error message related to the sign-in password.
 * @property signInPasswordVisibility The visibility state of sign-in password.
 * @property signUpEmail The email entered for sign-up.
 * @property signUpEmailError The error message related to the sign-up email.
 * @property signUpPassword The password entered for sign-up.
 * @property signUpPasswordError The error message related to the sign-up password.
 * @property signUpPasswordVisibility The visibility state of sign-up password.
 * @property signUpPasswordConfirmationError The error message related to the sign-up password confirmation.
 * @property signUpPasswordConfirmation The password confirmation entered for sign-up.
 * @property isUserSignedUp A flag indicating if the user has successfully signed up.
 * @property resetPasswordEmail The email entered for resetting the password.
 * @property resetPasswordEmailError The error message related to the reset password email.
 * @property isLoading A flag indicating if a process is currently loading.
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
    val isUserSignedUp: Boolean = false,
    val resetPasswordEmail: String = "",
    val resetPasswordEmailError: String? = null,
    val isLoading: Boolean = false
)