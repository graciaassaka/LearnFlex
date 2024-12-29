package org.example.shared.presentation.state

import org.example.shared.presentation.util.AuthForm

/**
 * Data class representing the state of the authentication UI.
 *
 * @property currentForm The current form being displayed.
 * @property signInEmail The email entered for signing in.
 * @property signInEmailError The error message for the sign-in email.
 * @property signInPassword The password entered for signing in.
 * @property signInPasswordError The error message for the sign-in password.
 * @property signInPasswordVisibility The visibility of the sign-in password.
 * @property signUpEmail The email entered for signing up.
 * @property signUpEmailError The error message for the sign-up email.
 * @property signUpPassword The password entered for signing up.
 * @property signUpPasswordError The error message for the sign-up password.
 * @property signUpPasswordVisibility The visibility of the sign-up password.
 * @property signUpPasswordConfirmationError The error message for the sign-up password confirmation.
 * @property signUpPasswordConfirmation The password entered for confirming the sign-up password.
 * @property resetPasswordEmail The email entered for resetting the password.
 * @property resetPasswordEmailError The error message for the reset password email.
 * @property isUserSignedIn A flag indicating whether the user is signed in.
 * @property isUserSignedUp A flag indicating whether the user is signed up.
 * @property isEmailVerified A flag indicating whether the email is verified.
 * @property isPasswordResetEmailSent A flag indicating whether the password is reset.
 * @property isLoading A flag indicating whether the authentication process is loading.
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
    val resetPasswordEmail: String = "",
    val resetPasswordEmailError: String? = null,
    val isUserSignedIn: Boolean = false,
    val isUserSignedUp: Boolean = false,
    val isEmailVerified: Boolean = false,
    val isPasswordResetEmailSent: Boolean = false,
    val isLoading: Boolean = false
)