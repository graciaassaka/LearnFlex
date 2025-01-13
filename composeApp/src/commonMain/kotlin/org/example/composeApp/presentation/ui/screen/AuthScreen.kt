package org.example.composeApp.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.alexzhirkevich.compottie.*
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.action.AuthAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.AuthUIState
import org.example.composeApp.presentation.ui.component.AnimatedOutlinedTextField
import org.example.composeApp.presentation.ui.component.CustomVerticalScrollbar
import org.example.composeApp.presentation.ui.constant.TestTags
import org.example.composeApp.presentation.ui.dimension.Dimension
import org.example.composeApp.presentation.ui.dimension.Padding
import org.example.composeApp.presentation.ui.dimension.Spacing
import org.example.composeApp.presentation.ui.layout.AuthLayout
import org.example.composeApp.presentation.ui.util.HandleUIEvents
import org.example.composeApp.presentation.ui.util.ScreenConfig
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.viewModel.AuthViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Enum class representing the authentication form.
 */
enum class AuthForm {
    SignIn,
    SignUp,
    ResetPassword,
    VerifyEmail
}

/**
 * Represents the different phases of the authentication UI.
 */
private enum class AuthUiPhase {
    Form,
    Animation,
    Hidden
}

/**
 * Represents the authentication screen which handles different authentication forms
 * such as SignIn, SignUp, VerifyEmail, and ForgotPassword based on the current UI state.
 *
 * @param windowSizeClass The size class of the window, used to adjust UI components accordingly.
 * @param navController The navigation controller to handle navigation actions between screens.
 * @param viewModel The authentication view model, providing the state and actions for the screen.
 */
@Composable
fun AuthScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: AuthViewModel = koinViewModel()
) {
    val screenConfig = ScreenConfig(
        windowSizeClass = windowSizeClass,
        snackbarHostState = remember { SnackbarHostState() },
        snackbarType = remember { mutableStateOf(SnackbarType.Info) },
        uiState = viewModel.state.collectAsStateWithLifecycle(),
        isScreenVisible = viewModel.isScreenVisible.collectAsStateWithLifecycle()
    )

    HandleUIEvents(Route.Auth, navController, viewModel, screenConfig.snackbarHostState) { screenConfig.snackbarType.value = it }

    when (screenConfig.uiState.value.currentForm) {
        AuthForm.SignIn -> SignInForm(
            screenConfig = screenConfig,
            handleAction = viewModel::handleAction,
            modifier = Modifier.testTag(TestTags.SIGN_IN_FORM.tag)
        )

        AuthForm.SignUp -> SignUpForm(
            screenConfig = screenConfig,
            handleAction = viewModel::handleAction,
            modifier = Modifier.testTag(TestTags.SIGN_UP_FORM.tag)
        )

        AuthForm.VerifyEmail -> {
            VerificationForm(
                screenConfig = screenConfig,
                handleAction = viewModel::handleAction,
                modifier = Modifier.testTag(TestTags.VERIFY_EMAIL_FORM.tag)
            )
        }

        AuthForm.ResetPassword -> {
            PasswordResetForm(
                screenConfig = screenConfig,
                handleAction = viewModel::handleAction,
                modifier = Modifier.testTag(TestTags.RESET_PASSWORD_FORM.tag)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SignInForm(
    screenConfig: ScreenConfig<AuthUIState>,
    handleAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var authUiPhase by remember { mutableStateOf(AuthUiPhase.Form) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }
    val scrollState = rememberScrollState()
    val lockAnimComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/lock.json").decodeToString()
        )
    }
    val lockAnimProgress = animateLottieCompositionAsState(
        composition = lockAnimComposition,
        iterations = 1,
        isPlaying = authUiPhase == AuthUiPhase.Animation
    )
    LaunchedEffect(uiState.value.isUserSignedIn) {
        if (uiState.value.isUserSignedIn) authUiPhase = AuthUiPhase.Animation
    }
    LaunchedEffect(lockAnimProgress.progress) {
        if (lockAnimProgress.progress >= 1f) authUiPhase = AuthUiPhase.Hidden
    }

    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        enabled = !uiState.value.isLoading,
        isVisible = authUiPhase != AuthUiPhase.Hidden,
        onAnimationFinished = {
            currentDestination?.let { form -> handleAction(AuthAction.DisplayAuthForm(form)) }
                ?: handleAction(AuthAction.HandleAnimationEnd)
        }
    ) {
        when (authUiPhase) {
            AuthUiPhase.Hidden -> Unit

            AuthUiPhase.Animation -> LottieAnimationBox(
                composition = lockAnimComposition,
                animationState = lockAnimProgress,
                modifier = Modifier.fillMaxSize()
            )

            AuthUiPhase.Form -> AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    CustomVerticalScrollbar(
                        scrollState = scrollState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Padding.MEDIUM.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                        Text(
                            text = stringResource(Res.string.sign_in_screen_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag(TestTags.SIGN_IN_SCREEN_TITLE.tag)
                        )
                        EmailInputField(
                            email = uiState.value.signInEmail,
                            onEmailChanged = { handleAction(AuthAction.EditSignInEmail(it)) },
                            emailError = uiState.value.signInEmailError,
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_IN_EMAIL_FIELD.tag)
                        )
                        PasswordInputField(
                            password = uiState.value.signInPassword,
                            onPasswordChanged = { handleAction(AuthAction.EditSignInPassword(it)) },
                            passwordError = uiState.value.signInPasswordError,
                            enabled = !uiState.value.isLoading,
                            onPasswordVisibilityToggled = { handleAction(AuthAction.ToggleSignInPasswordVisibility) },
                            passwordVisibility = uiState.value.signInPasswordVisibility,
                            modifier = Modifier.testTag(TestTags.SIGN_IN_PASSWORD_FIELD.tag),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions { handleAction(AuthAction.SignIn) }
                        )
                        Button(
                            onClick = { handleAction(AuthAction.SignIn) },
                            enabled = !uiState.value.isLoading &&
                                    uiState.value.signInEmailError.isNullOrBlank() &&
                                    uiState.value.signInPasswordError.isNullOrBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                                .testTag(TestTags.SIGN_IN_BUTTON.tag),
                            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                            content = { Text(stringResource(Res.string.sign_in_button_label)) }
                        )
                        TextButton(
                            onClick = {
                                authUiPhase = AuthUiPhase.Hidden
                                currentDestination = AuthForm.ResetPassword
                            },
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_IN_FORGOT_PASSWORD_BUTTON.tag),
                            content = { Text(stringResource(Res.string.forgot_password_button_label)) }
                        )
                        AuthDivider()
                        TextButton(
                            onClick = {
                                authUiPhase = AuthUiPhase.Hidden
                                currentDestination = AuthForm.SignUp
                            },
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_IN_CREATE_ACCOUNT_BUTTON.tag),
                            content = { Text(stringResource(Res.string.create_account_button_label)) }
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SignUpForm(
    screenConfig: ScreenConfig<AuthUIState>,
    handleAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var authUiPhase by remember { mutableStateOf(AuthUiPhase.Form) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }
    val scrollState = rememberScrollState()
    val lockAnimComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/email_confirmation.json").decodeToString()
        )
    }
    val lockAnimProgress = animateLottieCompositionAsState(
        composition = lockAnimComposition,
        iterations = 1,
        isPlaying = authUiPhase == AuthUiPhase.Animation
    )
    LaunchedEffect(uiState.value.isUserSignedUp) {
        if (uiState.value.isUserSignedUp) {
            authUiPhase = AuthUiPhase.Animation
        }
    }
    LaunchedEffect(lockAnimProgress.progress) {
        if (lockAnimProgress.progress == 1f) {
            authUiPhase = AuthUiPhase.Hidden
            currentDestination = AuthForm.VerifyEmail
        }
    }
    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        enabled = !uiState.value.isLoading,
        isVisible = authUiPhase != AuthUiPhase.Hidden,
        onAnimationFinished = {
            currentDestination?.let { form -> handleAction(AuthAction.DisplayAuthForm(form)) }
                ?: handleAction(AuthAction.HandleAnimationEnd)
        }
    ) {
        when (authUiPhase) {
            AuthUiPhase.Hidden -> Unit

            AuthUiPhase.Animation -> LottieAnimationBox(
                composition = lockAnimComposition,
                animationState = lockAnimProgress,
                modifier = Modifier.fillMaxSize()
            )

            AuthUiPhase.Form -> AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    CustomVerticalScrollbar(
                        scrollState = scrollState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Padding.MEDIUM.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                        Text(
                            text = stringResource(Res.string.sign_up_screen_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag(TestTags.SIGN_UP_SCREEN_TITLE.tag)
                        )
                        EmailInputField(
                            email = uiState.value.signUpEmail,
                            onEmailChanged = { handleAction(AuthAction.EditSignUpEmail(it)) },
                            emailError = uiState.value.signUpEmailError,
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_UP_EMAIL_FIELD.tag)
                        )
                        PasswordInputField(
                            password = uiState.value.signUpPassword,
                            onPasswordChanged = { handleAction(AuthAction.EditSignUpPassword(it)) },
                            passwordError = uiState.value.signUpPasswordError,
                            enabled = !uiState.value.isLoading,
                            passwordVisibility = uiState.value.signUpPasswordVisibility,
                            onPasswordVisibilityToggled = { handleAction(AuthAction.ToggleSignUpPasswordVisibility) },
                            modifier = Modifier.testTag(TestTags.SIGN_UP_PASSWORD_FIELD.tag)
                        )
                        ConfirmPasswordInputField(
                            password = uiState.value.signUpPasswordConfirmation,
                            onPasswordChanged = { handleAction(AuthAction.EditSignUpPasswordConfirmation(it)) },
                            passwordError = uiState.value.signUpPasswordConfirmationError,
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_UP_CONFIRM_PASSWORD_FIELD.tag),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions { handleAction(AuthAction.SignUp) }
                        )
                        Button(
                            onClick = { handleAction(AuthAction.SignUp) },
                            enabled = !uiState.value.isLoading &&
                                    uiState.value.signUpEmailError.isNullOrBlank() &&
                                    uiState.value.signUpPasswordError.isNullOrBlank() &&
                                    uiState.value.signUpPasswordConfirmationError.isNullOrBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                                .testTag(TestTags.SIGN_UP_BUTTON.tag),
                            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                            content = { Text(stringResource(Res.string.sign_up_button_label)) }
                        )
                        AuthDivider()
                        TextButton(
                            onClick = {
                                authUiPhase = AuthUiPhase.Hidden
                                currentDestination = AuthForm.SignIn
                            },
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.SIGN_UP_SIGN_IN_BUTTON.tag),
                            content = { Text(stringResource(Res.string.already_have_account_button_label)) }
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun VerificationForm(
    screenConfig: ScreenConfig<AuthUIState>,
    handleAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var authUiPhase by remember { mutableStateOf(AuthUiPhase.Form) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }
    val scrollState = rememberScrollState()
    val scanUnlockComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/scan_and_unlock.json").decodeToString()
        )
    }
    val scanUnlockAnimationState = animateLottieCompositionAsState(
        composition = scanUnlockComposition,
        iterations = 1,
        isPlaying = authUiPhase == AuthUiPhase.Animation
    )
    val twoFactorAuthComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/two_factor_authentication.json").decodeToString()
        )
    }
    val twoFactorAuthAnimationState = animateLottieCompositionAsState(
        composition = twoFactorAuthComposition,
        iterations = Compottie.IterateForever
    )
    LaunchedEffect(uiState.value.isEmailVerified) {
        if (uiState.value.isEmailVerified) authUiPhase = AuthUiPhase.Animation
    }
    LaunchedEffect(scanUnlockAnimationState.progress) {
        if (scanUnlockAnimationState.progress == 1f) authUiPhase = AuthUiPhase.Hidden
    }

    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        enabled = !uiState.value.isLoading,
        isVisible = authUiPhase != AuthUiPhase.Hidden,
        onAnimationFinished = {
            currentDestination?.let { form -> handleAction(AuthAction.DisplayAuthForm(form)) }
                ?: handleAction(AuthAction.HandleAnimationEnd)
        }
    ) {
        when (authUiPhase) {
            AuthUiPhase.Hidden -> Unit

            AuthUiPhase.Animation -> LottieAnimationBox(
                composition = scanUnlockComposition,
                animationState = scanUnlockAnimationState,
                modifier = Modifier.fillMaxSize()
            )

            AuthUiPhase.Form -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(modifier = modifier.fillMaxSize()) {
                        CustomVerticalScrollbar(
                            scrollState = scrollState,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Padding.MEDIUM.dp)
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                            Text(
                                text = stringResource(Res.string.verify_email_screen_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag(TestTags.VERIFY_EMAIL_SCREEN_TITLE.tag)
                            )
                            Text(
                                text = stringResource(Res.string.verify_email_screen_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag(TestTags.VERIFY_EMAIL_SCREEN_DESCRIPTION.tag)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.value.signUpEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.testTag(TestTags.VERIFY_EMAIL_SCREEN_EMAIL.tag)
                                )
                                IconButton(
                                    onClick = {
                                        authUiPhase = AuthUiPhase.Hidden
                                        handleAction(AuthAction.DeleteUser)
                                        currentDestination = AuthForm.SignUp
                                    },
                                    modifier = Modifier.testTag(TestTags.VERIFY_EMAIL_SCREEN_EDIT_EMAIL_BUTTON.tag),
                                    enabled = !uiState.value.isLoading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.email_label),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Image(
                                painter = rememberLottiePainter(
                                    composition = twoFactorAuthComposition,
                                    progress = twoFactorAuthAnimationState::value
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(150.dp)
                            )
                            Button(
                                onClick = { handleAction(AuthAction.VerifyEmail) },
                                enabled = !uiState.value.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                                    .testTag(TestTags.VERIFY_EMAIL_SCREEN_VERIFY_EMAIL_BUTTON.tag),
                                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                                content = { Text(stringResource(Res.string.verify_email_button_label)) }
                            )
                            OutlinedButton(
                                onClick = { handleAction(AuthAction.ResendVerificationEmail) },
                                enabled = !uiState.value.isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                                    .testTag(TestTags.VERIFY_EMAIL_RESEND_EMAIL_BUTTON.tag),
                                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                                content = { Text(stringResource(Res.string.resend_email_button_label)) }
                            )
                            Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun PasswordResetForm(
    screenConfig: ScreenConfig<AuthUIState>,
    handleAction: (AuthAction) -> Unit,
    modifier: Modifier = Modifier
) = with(screenConfig) {
    var authUiPhase by remember { mutableStateOf(AuthUiPhase.Form) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }
    val scrollState = rememberScrollState()
    val emailSentAnimation by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/email_sent.json").decodeToString()
        )
    }
    val emailSentAnimationState = animateLottieCompositionAsState(
        composition = emailSentAnimation,
        iterations = 1,
        isPlaying = authUiPhase == AuthUiPhase.Animation
    )
    val forgotPasswordComposition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/forgot_password.json").decodeToString()
        )
    }
    val forgotPasswordAnimationState = animateLottieCompositionAsState(
        composition = forgotPasswordComposition,
        iterations = Compottie.IterateForever
    )
    LaunchedEffect(uiState.value.isPasswordResetEmailSent) {
        if (uiState.value.isPasswordResetEmailSent) authUiPhase = AuthUiPhase.Animation
    }
    LaunchedEffect(emailSentAnimationState.progress) {
        if (emailSentAnimationState.progress == 1f) {
            authUiPhase = AuthUiPhase.Hidden
            currentDestination = AuthForm.SignIn
        }
    }

    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType.value,
        enabled = !uiState.value.isLoading,
        isVisible = authUiPhase != AuthUiPhase.Hidden,
        onAnimationFinished = {
            currentDestination?.let { form -> handleAction(AuthAction.DisplayAuthForm(form)) }
                ?: handleAction(AuthAction.HandleAnimationEnd)
        }
    ) {
        when (authUiPhase) {
            AuthUiPhase.Hidden -> Unit

            AuthUiPhase.Animation -> LottieAnimationBox(
                composition = emailSentAnimation,
                animationState = emailSentAnimationState,
                modifier = Modifier.fillMaxSize()
            )

            AuthUiPhase.Form -> AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(modifier = modifier.fillMaxSize()) {
                    CustomVerticalScrollbar(
                        scrollState = scrollState,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = Padding.MEDIUM.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                        Image(
                            painter = rememberLottiePainter(
                                composition = forgotPasswordComposition,
                                progress = forgotPasswordAnimationState::value
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp)
                        )
                        Text(
                            text = stringResource(Res.string.password_reset_screen_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag(TestTags.RESET_PASSWORD_SCREEN_TITLE.tag)
                        )
                        EmailInputField(
                            email = uiState.value.resetPasswordEmail,
                            onEmailChanged = { handleAction(AuthAction.EditPasswordResetEmail(it)) },
                            emailError = uiState.value.resetPasswordEmailError,
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.RESET_PASSWORD_EMAIL_FIELD.tag),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions { handleAction(AuthAction.SendPasswordResetEmail) }
                        )
                        Button(
                            onClick = { handleAction(AuthAction.SendPasswordResetEmail) },
                            enabled = !uiState.value.isLoading && uiState.value.resetPasswordEmailError.isNullOrBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                                .testTag(TestTags.RESET_PASSWORD_BUTTON.tag),
                            shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                            content = { Text(stringResource(Res.string.send_reset_password_email_button_label)) }
                        )
                        AuthDivider()
                        TextButton(
                            onClick = {
                                authUiPhase = AuthUiPhase.Hidden
                                currentDestination = AuthForm.SignIn
                            },
                            enabled = !uiState.value.isLoading,
                            modifier = Modifier.testTag(TestTags.RESET_PASSWORD_SIGN_IN_BUTTON.tag),
                            content = { Text(stringResource(Res.string.back_to_sign_in_button_label)) }
                        )
                        Spacer(modifier = Modifier.Companion.height(Spacing.LARGE.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmailInputField(
    email: String,
    onEmailChanged: (String) -> Unit,
    emailError: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) = AnimatedOutlinedTextField(
    value = email,
    onValueChange = onEmailChanged,
    enabled = enabled,
    modifier = modifier,
    label = { Text(stringResource(Res.string.email_label)) },
    leadingIcon = { Icon(Icons.Default.Email, null) },
    supportingText = { Text(emailError ?: "", color = MaterialTheme.colorScheme.error) },
    isError = emailError.isNullOrBlank().not(),
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions
)

@Composable
private fun PasswordInputField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    passwordError: String?,
    enabled: Boolean,
    passwordVisibility: Boolean,
    onPasswordVisibilityToggled: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default
) = AnimatedOutlinedTextField(
    value = password,
    onValueChange = onPasswordChanged,
    enabled = enabled,
    modifier = modifier,
    label = { Text(stringResource(Res.string.password_label)) },
    supportingText = { Text(passwordError ?: "", color = MaterialTheme.colorScheme.error) },
    isError = passwordError.isNullOrBlank().not(),
    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
    leadingIcon = { Icon(Icons.Default.Lock, null) },
    trailingIcon = {
        IconButton(
            onClick = onPasswordVisibilityToggled,
            modifier = Modifier.testTag(TestTags.TOGGLE_PASSWORD_VISIBILITY.tag)
        ) {
            Icon(
                imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = stringResource(Res.string.toggle_password_visibility)
            )
        }
    },
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions
)

@Composable
private fun ConfirmPasswordInputField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    passwordError: String?,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
) = AnimatedOutlinedTextField(
    value = password,
    onValueChange = onPasswordChanged,
    enabled = enabled,
    modifier = modifier,
    label = { Text(stringResource(Res.string.confirm_password_label)) },
    supportingText = { Text(passwordError ?: "", color = MaterialTheme.colorScheme.error) },
    isError = passwordError.isNullOrBlank().not(),
    visualTransformation = PasswordVisualTransformation(),
    leadingIcon = { Icon(Icons.Default.Lock, null) },
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions
)

@Composable
private fun AuthDivider(
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
) {
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
    Text(
        text = stringResource(Res.string.or_label),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.Companion.padding(Padding.MEDIUM.dp)
    )
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}

@Composable
private fun LottieAnimationBox(
    composition: LottieComposition?,
    animationState: LottieAnimationState,
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center
) {
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = animationState::value
        ),
        contentDescription = null,
        modifier = Modifier.size(200.dp)
    )
}
