package org.example.composeApp.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.component.AnimatedOutlinedTextField
import org.example.composeApp.component.HandleUIEvents
import org.example.composeApp.dimension.Dimension
import org.example.composeApp.dimension.Padding
import org.example.composeApp.dimension.Spacing
import org.example.composeApp.layout.AuthLayout
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.viewModel.AuthViewModel
import org.example.shared.util.AuthForm
import org.example.shared.util.SnackbarType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Displays the authentication screen for the application.
 *
 * @param navController The navigation controller used for navigating between screens.
 * @param viewModel The view model used for handling the authentication logic.
 */
@Composable
fun AuthScreen(
    windowSizeClass: WindowSizeClass,
    navController: NavController,
    viewModel: AuthViewModel = koinViewModel()
)
{
    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by remember { mutableStateOf<SnackbarType>(SnackbarType.Info) }
    HandleUIEvents(Route.Auth, navController, viewModel, snackbarHostState) { currentSnackbarType = it }
    val uiState by viewModel.state.collectAsState()
    val isScreenVisible by viewModel.isScreenVisible.collectAsState()

    when (uiState.currentForm)
    {
        AuthForm.SignIn -> SignInForm(
            isScreenVisible = isScreenVisible,
            windowSizeClass = windowSizeClass,
            snackbarHostState = snackbarHostState,
            snackbarType = currentSnackbarType,
            email = uiState.signInEmail,
            onEmailChanged = viewModel::onSignInEmailChanged,
            emailError = uiState.signInEmailError,
            password = uiState.signInPassword,
            passwordError = uiState.signInPasswordError,
            onPasswordChanged = viewModel::onSignInPasswordChanged,
            passwordVisibility = uiState.signInPasswordVisibility,
            onPasswordVisibilityToggled = viewModel::toggleSignInPasswordVisibility,
            onSignInClicked = viewModel::signIn,
            enabled = !uiState.isLoading,
            displayAuthForm = viewModel::displayAuthForm,
            onAnimationFinished = viewModel::onExitAnimationFinished,
            modifier = Modifier.testTag("sign_in_form")
        )

        AuthForm.SignUp -> SignUpForm(
            isScreenVisible = isScreenVisible,
            windowSizeClass = windowSizeClass,
            snackbarHostState = snackbarHostState,
            snackbarType = currentSnackbarType,
            email = uiState.signUpEmail,
            onEmailChanged = viewModel::onSignUpEmailChanged,
            emailError = uiState.signUpEmailError,
            password = uiState.signUpPassword,
            passwordError = uiState.signUpPasswordError,
            onPasswordChanged = viewModel::onSignUpPasswordChanged,
            passwordVisibility = uiState.signUpPasswordVisibility,
            onPasswordVisibilityToggled = viewModel::toggleSignUpPasswordVisibility,
            confirmedPassword = uiState.signUpPasswordConfirmation,
            confirmedPasswordError = uiState.signUpPasswordConfirmationError,
            onConfirmedPasswordChanged = viewModel::onSignUpPasswordConfirmationChanged,
            onSignUpClicked = viewModel::signUp,
            enabled = !uiState.isLoading,
            displayAuthForm = viewModel::displayAuthForm,
            onAnimationFinished = viewModel::onExitAnimationFinished,
            modifier = Modifier.testTag("sign_up_form")
        )

        AuthForm.ForgotPassword ->
        {

        }
    }
}

@Composable
private fun SignInForm(
    isScreenVisible: Boolean,
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    email: String,
    onEmailChanged: (String) -> Unit,
    emailError: String?,
    password: String,
    passwordError: String?,
    onPasswordChanged: (String) -> Unit,
    passwordVisibility: Boolean,
    onPasswordVisibilityToggled: () -> Unit,
    onSignInClicked: () -> Unit,
    enabled: Boolean,
    displayAuthForm: (AuthForm) -> Unit,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
)
{
    var isFormVisible by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }

    LaunchedEffect(isScreenVisible) { isFormVisible = isScreenVisible }

    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType,
        isVisible = isFormVisible,
        onAnimationFinished = {
            if (currentDestination == null) onAnimationFinished()
            else displayAuthForm(currentDestination!!)
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Padding.MEDIUM.dp, Padding.LARGE.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.sign_in_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("sign_in_screen_title")
            )
            EmailInputField(
                email = email,
                onEmailChanged = onEmailChanged,
                emailError = emailError,
                enabled = enabled,
                modifier = Modifier.testTag("sign_in_email_field")
            )
            PasswordInputField(
                password = password,
                onPasswordChanged = onPasswordChanged,
                passwordError = passwordError,
                enabled = enabled,
                onPasswordVisibilityToggled = onPasswordVisibilityToggled,
                passwordVisibility = passwordVisibility,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { onSignInClicked() },
                modifier = Modifier.testTag("sign_in_password_field")
            )
            Button(
                onClick = onSignInClicked,
                enabled = enabled && emailError.isNullOrBlank() && passwordError.isNullOrBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                    .testTag("sign_in_button"),
                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                content = { Text(stringResource(Res.string.sign_in_button_label)) }
            )
            TextButton(
                onClick = {
                    isFormVisible = false
                    currentDestination = AuthForm.ForgotPassword
                },
                enabled = enabled,
                modifier = Modifier.testTag("sign_in_forgot_password_button"),
                content = { Text(stringResource(Res.string.forgot_password_button_label)) }
            )
            AuthDivider()
            TextButton(
                onClick = {
                    isFormVisible = false
                    currentDestination = AuthForm.SignUp
                },
                enabled = enabled,
                modifier = Modifier.testTag("sign_in_create_account_button"),
                content = { Text(stringResource(Res.string.create_account_button_label)) }
            )
        }
    }
}

@Composable
private fun SignUpForm(
    isScreenVisible: Boolean,
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    snackbarType: SnackbarType,
    email: String,
    onEmailChanged: (String) -> Unit,
    emailError: String?,
    password: String,
    passwordError: String?,
    onPasswordChanged: (String) -> Unit,
    passwordVisibility: Boolean,
    onPasswordVisibilityToggled: () -> Unit,
    confirmedPassword: String,
    confirmedPasswordError: String?,
    onConfirmedPasswordChanged: (String) -> Unit,
    onSignUpClicked: () -> Unit,
    enabled: Boolean,
    displayAuthForm: (AuthForm) -> Unit,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
)
{
    var isFormVisible by remember { mutableStateOf(true) }
    var currentDestination by remember { mutableStateOf<AuthForm?>(null) }

    LaunchedEffect(isScreenVisible) { isFormVisible = isScreenVisible }

    AuthLayout(
        windowSizeClass = windowSizeClass,
        snackbarHostState = snackbarHostState,
        snackbarType = snackbarType,
        isVisible = isFormVisible,
        onAnimationFinished = {
            if (currentDestination == null) onAnimationFinished()
            else displayAuthForm(currentDestination!!)
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Padding.MEDIUM.dp, Padding.LARGE.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.sign_up_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("sign_up_screen_title")
            )
            EmailInputField(
                email = email,
                onEmailChanged = onEmailChanged,
                emailError = emailError,
                enabled = enabled,
                modifier = Modifier.testTag("sign_up_email_field")
            )
            PasswordInputField(
                password = password,
                onPasswordChanged = onPasswordChanged,
                passwordError = passwordError,
                enabled = enabled,
                passwordVisibility = passwordVisibility,
                onPasswordVisibilityToggled = onPasswordVisibilityToggled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions { onSignUpClicked() },
                modifier = Modifier.testTag("sign_up_password_field")
            )
            ConfirmPasswordInputField(
                password = confirmedPassword,
                onPasswordChanged = onConfirmedPasswordChanged,
                passwordError = confirmedPasswordError,
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { onSignUpClicked() },
                modifier = Modifier.testTag("sign_up_confirm_password_field")
            )
            Button(
                onClick = onSignUpClicked,
                enabled = enabled && emailError.isNullOrBlank() && passwordError.isNullOrBlank() && confirmedPasswordError.isNullOrBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimension.AUTH_BUTTON_HEIGHT.dp)
                    .testTag("sign_up_button"),
                shape = RoundedCornerShape(Dimension.CORNER_RADIUS_LARGE.dp),
                content = { Text(stringResource(Res.string.sign_up_button_label)) }
            )
            AuthDivider()
            TextButton(
                onClick = {
                    isFormVisible = false
                    currentDestination = AuthForm.SignIn
                },
                enabled = enabled,
                modifier = Modifier.testTag("sign_up_sign_in_button"),
                content = { Text(stringResource(Res.string.already_have_account_button_label)) }
            )
        }
    }
}

@Composable
private fun EmailInputField(
    email: String,
    onEmailChanged: (String) -> Unit,
    emailError: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) = AnimatedOutlinedTextField(
    value = email,
    onValueChange = onEmailChanged,
    enabled = enabled,
    modifier = modifier,
    label = { Text(stringResource(Res.string.email_label)) },
    leadingIcon = { Icon(Icons.Default.Email, null) },
    supportingText = { Text(emailError ?: "", color = MaterialTheme.colorScheme.error) },
    isError = emailError.isNullOrBlank().not(),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
)

@Composable
private fun PasswordInputField(
    password: String,
    onPasswordChanged: (String) -> Unit,
    passwordError: String?,
    enabled: Boolean,
    passwordVisibility: Boolean,
    onPasswordVisibilityToggled: () -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier
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
            modifier = Modifier.testTag("toggle_password_visibility")
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
        modifier = Modifier.padding(Padding.MEDIUM.dp)
    )
    HorizontalDivider(
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    )
}
