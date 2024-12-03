package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.shared.domain.constant.Style
import org.example.shared.domain.constant.SyncStatus
import org.example.shared.domain.data_source.PathBuilder
import org.example.shared.domain.model.*
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.*
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.ProfileCreationForm
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult

/**
 * ViewModel for the profile creation screen.
 *
 * @param getUserDataUseCase The use case to get the user data.
 * @param createUserProfileUseCase The use case to create a user profile.
 * @param uploadProfilePictureUseCase The use case to upload a profile picture.
 * @param deleteProfilePictureUseCase The use case to delete a profile picture.
 * @param getStyleQuestionnaireUseCase The use case to get the style questionnaire.
 * @param getStyleResultUseCase The use case to get the style result.
 * @param createUserStyleUseCase The use case to set the user style.
 * @param pathBuilder The path builder for user profiles.
 * @param syncManager The sync manager for user profiles.
 * @param dispatcher The coroutine dispatcher to run the use cases on.
 * @param sharingStarted The sharing strategy for the state flow.
 */
class CreateUserProfileViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val getStyleQuestionnaireUseCase: GetStyleQuestionnaireUseCase,
    private val getStyleResultUseCase: GetStyleResultUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val pathBuilder: PathBuilder,
    private val syncManager: SyncManager<UserProfile>,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher) {
    // The number of questions to be fetched in the style questionnaire
    val questionCount = 5

    // Mutable state flow to manage the UI state of the profile creation screen
    private val _state = MutableStateFlow(CreateProfileUIState())
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    // Collects sync status updates and handles errors
    init {
        viewModelScope.launch {
            syncManager.syncStatus.collect { if (it is SyncStatus.Error) handleError(it.error) }
        }
    }

    /**
     * Fetches the user data from the database.
     */
    fun getUserData() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            getUserDataUseCase()
                .onSuccess { userData ->
                    update {
                        it.copy(
                            userId = userData.localId ?: "",
                            username = userData.displayName ?: "",
                            email = userData.email ?: "",
                        )
                    }
                }.onFailure {
                    handleError(it)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles changes to the username input.
     *
     * @param username The new username input.
     */
    fun onUsernameChanged(username: String) = _state.update {
        with(InputValidator.validateUsername(username)) {
            when (this@with) {
                is ValidationResult.Valid   -> it.copy(username = value, usernameError = null)
                is ValidationResult.Invalid -> it.copy(username = username, usernameError = message)
            }
        }
    }

    /**
     * Handles changes to the learning field selection.
     *
     * @param field The selected learning field.
     */
    fun onFieldChanged(field: Field) = _state.update { it.copy(field = field) }

    /**
     * Handles changes to the learning level selection.
     *
     * @param level The selected learning level.
     */
    fun onLevelChanged(level: Level) = _state.update { it.copy(level = level) }

    /**
     * Handles changes to the learning level dropdown visibility.
     */
    fun toggleLevelDropdownVisibility() =
        _state.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }

    /**
     * Handles changes to the learning goal input.
     *
     * @param goal The new learning goal input.
     */
    fun onGoalChanged(goal: String) = _state.update { it.copy(goal = goal) }

    /**
     * Handles the upload of a profile picture.
     *
     * @param imageData The image data of the profile picture.
     * @param successMessage The message to show on successful upload.
     */
    fun onUploadProfilePicture(imageData: ByteArray, successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            uploadProfilePictureUseCase(imageData)
                .onSuccess { url ->
                    update { it.copy(photoUrl = url) }
                    showSnackbar(successMessage, SnackbarType.Success)
                }.onFailure { error ->
                    handleError(error)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles the deletion of a profile picture.
     *
     * @param successMessage The message to show on successful deletion.
     */
    fun onProfilePictureDeleted(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            deleteProfilePictureUseCase()
                .onSuccess {
                    update { it.copy(photoUrl = "") }
                    showSnackbar(successMessage, SnackbarType.Success)
                }.onFailure { error ->
                    handleError(error)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles the creation of a user profile.
     *
     * @param successMessage The message to show on successful profile creation.
     */
    fun onCreateProfile(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        onUsernameChanged(value.username)

        if (value.usernameError.isNullOrBlank()) viewModelScope.launch(dispatcher) {
            createUserProfileUseCase(
                userProfile = UserProfile(
                    id = value.userId,
                    email = value.email,
                    username = value.username,
                    photoUrl = value.photoUrl,
                    preferences = LearningPreferences(value.field.name, value.level.name, value.goal),
                    learningStyle = LearningStyle(),
                    createdAt = System.currentTimeMillis(),
                    lastUpdated = System.currentTimeMillis()
                ),
                path = pathBuilder.buildUserPath()
            ).onSuccess {
                showSnackbar(successMessage, SnackbarType.Success)
                update { it.copy(isProfileCreated = true) }
            }.onFailure { error ->
                handleError(error)
            }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Starts the style questionnaire process.
     * Initializes the state and fetches the style questionnaire.
     */
    fun startStyleQuestionnaire() = with(_state) {
        update {
            it.copy(
                isLoading = true,
                styleQuestionnaire = emptyList(),
                styleResponses = emptyList(),
                learningStyle = null,
                showStyleResultDialog = false
            )
        }

        viewModelScope.launch(dispatcher) {
            getStyleQuestionnaireUseCase(
                LearningPreferences(value.field.name, value.level.name, value.goal),
                questionCount
            )
                .collect { result ->
                    result.fold(
                        onSuccess = { question ->
                            update { state -> state.copy(styleQuestionnaire = state.styleQuestionnaire + question) }
                        },
                        onFailure = { error ->
                            handleError(error)
                        }
                    )
                    update { it.copy(isLoading = false) }
                }
        }
    }

    /**
     * Handles the event when a question is answered in the style questionnaire.
     *
     * @param style The style response to the question.
     */
    fun onQuestionAnswered(style: Style) = _state.update { it.copy(styleResponses = it.styleResponses + style) }

    /**
     * Completes the style questionnaire and fetches the style result.
     */
    fun onQuestionnaireCompleted() = with(_state) {
        getStyleResultUseCase(value.styleResponses)
            .onSuccess { result -> update { it.copy(learningStyle = result, showStyleResultDialog = true) } }
            .onFailure { error -> handleError(error) }
    }

    /**
     * Sets the learning style for the user.
     *
     * @param successMessage The message to show on successful setting of the learning style.
     */
    fun setLearningStyle(successMessage: String) = with(_state) {
        try {
            require(value.learningStyle != null)

            update { it.copy(isLoading = true) }
            viewModelScope.launch(dispatcher) {
                updateUserProfileUseCase(
                    userProfile = UserProfile(
                        id = value.userId,
                        email = value.email,
                        username = value.username,
                        photoUrl = value.photoUrl,
                        preferences = LearningPreferences(value.field.name, value.level.name, value.goal),
                        learningStyle = value.learningStyle!!,
                        createdAt = System.currentTimeMillis(),
                        lastUpdated = System.currentTimeMillis()
                    ),
                    path = pathBuilder.buildUserPath()
                ).onSuccess {
                    showSnackbar(successMessage, SnackbarType.Success)
                    navigate(Route.Dashboard, true)
                }.onFailure { error ->
                    handleError(error)
                }
            }
        } catch (e: IllegalArgumentException) {
            handleError(e)
        } finally {
            update { it.copy(showStyleResultDialog = false, isLoading = false) }
        }
    }

    /**
     * Displays the profile creation form.
     *
     * @param form The form to display.
     */
    fun displayProfileCreationForm(form: ProfileCreationForm) = _state.update {
        when (form) {
            ProfileCreationForm.PERSONAL_INFO       -> it.copy(currentForm = ProfileCreationForm.PERSONAL_INFO)
            ProfileCreationForm.STYLE_QUESTIONNAIRE -> it.copy(currentForm = ProfileCreationForm.STYLE_QUESTIONNAIRE)
        }
    }
}

