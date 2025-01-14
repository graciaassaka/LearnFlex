package org.example.composeApp.presentation.action

import org.example.composeApp.presentation.state.AuthUIState.AuthForm

/**
 * Sealed class representing the actions that can be performed on the authentication UI.
 */
sealed class AuthAction {

    /**
     * Action to handle changes in the sign-in email field.
     * @param email The new email entered by the user.
     */
    data class EditSignInEmail(val email: String) : AuthAction()

    /**
     * Action to handle changes in the sign-in password field.
     * @param password The new password entered by the user.
     */
    data class EditSignInPassword(val password: String) : AuthAction()

    /**
     * Action to handle changes in the password reset email field.
     * @param email The new email entered by the user for password reset.
     */
    data class EditPasswordResetEmail(val email: String) : AuthAction()

    /**
     * Action to handle changes in the sign-up email field.
     * @param email The new email entered by the user.
     */
    data class EditSignUpEmail(val email: String) : AuthAction()

    /**
     * Action to handle changes in the sign-up password confirmation field.
     * @param password The new password confirmation entered by the user.
     */
    data class EditSignUpPasswordConfirmation(val password: String) : AuthAction()

    /**
     * Action to handle changes in the sign-up password field.
     * @param password The new password entered by the user.
     */
    data class EditSignUpPassword(val password: String) : AuthAction()

    /**
     * Action to display the authentication form.
     * @param form The authentication form to display.
     */
    data class DisplayAuthForm(val form: AuthForm) : AuthAction()

    /**
     * Action to delete the user.
     */
    data object DeleteUser : AuthAction()

    /**
     * Action to handle the end of an animation.
     */
    data object HandleAnimationEnd : AuthAction()

    /**
     * Action to resend the verification email.
     */
    data object ResendVerificationEmail : AuthAction()

    /**
     * Action to send the password reset email.
     */
    data object SendPasswordResetEmail : AuthAction()

    /**
     * Action to handle the sign-in process.
     */
    data object SignIn : AuthAction()

    /**
     * Action to handle the sign-up process.
     */
    data object SignUp : AuthAction()

    /**
     * Action to toggle the visibility of the sign-in password.
     */
    data object ToggleSignInPasswordVisibility : AuthAction()

    /**
     * Action to toggle the visibility of the sign-up password.
     */
    data object ToggleSignUpPasswordVisibility : AuthAction()

    /**
     * Action to verify the email.
     */
    data object VerifyEmail : AuthAction()
}
