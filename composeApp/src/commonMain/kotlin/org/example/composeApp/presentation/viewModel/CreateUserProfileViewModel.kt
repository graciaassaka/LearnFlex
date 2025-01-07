package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.CreateProfileUIState
import org.example.composeApp.presentation.ui.screen.ProfileCreationForm
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.auth.GetUserDataUseCase
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.example.composeApp.presentation.action.CreateUserProfileAction as Action

/**
 * ViewModel for the profile creation screen.
 *
 * @param getUserDataUseCase The use case to get the user data.
 * @param createProfileUseCase The use case to create a user profile.
 * @param uploadProfilePictureUseCase The use case to upload a profile picture.
 * @param deleteProfilePictureUseCase The use case to delete a profile picture.
 * @param fetchStyleQuestionnaireUseCase The use case to get the style questionnaire.
 * @param getStyleResultUseCase The use case to get the style result.
 * @param createUserStyleUseCase The use case to set the user style.
 * @param syncManagers The list of sync managers to handle sync operations.
 * @param dispatcher The coroutine dispatcher to run the use cases on.
 * @param sharingStarted The sharing strategy for the state flow.
 */
class CreateUserProfileViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val fetchStyleQuestionnaireUseCase: FetchStyleQuestionnaireUseCase,
    private val getStyleResultUseCase: GetStyleResultUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val dispatcher: CoroutineDispatcher,
    syncManagers: List<SyncManager<DatabaseRecord>>,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher, syncManagers) {
    // Mutable state flow to manage the UI state of the profile creation screen
    private val _state = MutableStateFlow(CreateProfileUIState())
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    /**
     * Fetches the user data from the database.
     */
    private fun getUserData() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            getUserDataUseCase()
                .onSuccess { userData ->
                    update {
                        it.copy(
                            userId = userData.localId,
                            username = userData.displayName,
                            email = userData.email,
                        )
                    }
                }.onFailure {
                    handleError(it)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles the given action.
     *
     * @param action The action to handle.
     */
    fun handleAction(action: Action) {
        when (action) {
            is Action.EditUsername -> editUsername(action.username)
            is Action.SelectField -> selectField(action.field)
            is Action.SelectLevel -> SelectLevel(action.level)
            is Action.ToggleLevelDropdownVisibility -> toggleLevelDropdownVisibility()
            is Action.EditGoal -> editGoal(action.goal)
            is Action.UploadProfilePicture -> uploadProfilePicture(action.imageData, action.successMessage)
            is Action.DeleteProfilePicture -> DeleteProfilePicture(action.successMessage)
            is Action.CreateProfile -> createProfile(action.successMessage)
            is Action.StartStyleQuestionnaire -> startStyleQuestionnaire()
            is Action.HandleQuestionAnswered -> handleQuestionAnswered(action.style)
            is Action.HandleQuestionnaireCompleted -> handleQuestionnaireCompleted()
            is Action.SetLearningStyle -> setLearningStyle(action.successMessage)
            is Action.DisplayProfileCreationForm -> displayProfileCreationForm(action.form)
            is Action.HandleError -> handleError(action.error)
            is Action.HandleAnimationEnd -> handleExitAnimationFinished()
        }
    }

    /**
     * Handles changes to the username input.
     *
     * @param username The new username input.
     */
    private fun editUsername(username: String) = _state.update {
        with(validateUsernameUseCase(username)) {
            when (this@with) {
                is ValidationResult.Valid -> it.copy(username = value, usernameError = null)
                is ValidationResult.Invalid -> it.copy(username = username, usernameError = message)
            }
        }
    }

    /**
     * Handles changes to the learning field selection.
     *
     * @param field The selected learning field.
     */
    private fun selectField(field: Field) = _state.update { it.copy(field = field) }

    /**
     * Handles changes to the learning level selection.
     *
     * @param level The selected learning level.
     */
    private fun SelectLevel(level: Level) = _state.update { it.copy(level = level) }

    /**
     * Handles changes to the learning level dropdown visibility.
     */
    private fun toggleLevelDropdownVisibility() =
        _state.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }

    /**
     * Handles changes to the learning goal input.
     *
     * @param goal The new learning goal input.
     */
    private fun editGoal(goal: String) = _state.update { it.copy(goal = goal) }

    /**
     * Handles the upload of a profile picture.
     *
     * @param imageData The image data of the profile picture.
     * @param successMessage The message to show on successful upload.
     */
    private fun uploadProfilePicture(imageData: ByteArray, successMessage: String) = with(_state) {
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
    private fun DeleteProfilePicture(successMessage: String) = with(_state) {
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
    private fun createProfile(successMessage: String) = with(_state) {
        update { it.copy(isLoading = true) }

        editUsername(value.username)

        if (value.usernameError.isNullOrBlank()) viewModelScope.launch(dispatcher) {
            createProfileUseCase(
                Profile(
                    id = value.userId,
                    email = value.email,
                    username = value.username,
                    photoUrl = value.photoUrl,
                    preferences = Profile.LearningPreferences(value.field.name, value.level.name, value.goal),
                    learningStyle = Profile.LearningStyle(),
                    createdAt = System.currentTimeMillis(),
                    lastUpdated = System.currentTimeMillis()
                )
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
    private fun startStyleQuestionnaire() = with(_state) {
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
            fetchStyleQuestionnaireUseCase(
                Profile.LearningPreferences(value.field.name, value.level.name, value.goal),
                QUESTION_COUNT
            ).collect { result ->
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
    private fun handleQuestionAnswered(style: Style) = _state.update { it.copy(styleResponses = it.styleResponses + style) }

    /**
     * Completes the style questionnaire and fetches the style result.
     */
    private fun handleQuestionnaireCompleted() = with(_state) {
        getStyleResultUseCase(value.styleResponses)
            .onSuccess { result -> update { it.copy(learningStyle = result, showStyleResultDialog = true) } }
            .onFailure { error -> handleError(error) }
    }

    /**
     * Sets the learning style for the user.
     *
     * @param successMessage The message to show on successful setting of the learning style.
     */
    private fun setLearningStyle(successMessage: String) = with(_state) {
        try {
            require(value.learningStyle != null)

            update { it.copy(isLoading = true) }
            viewModelScope.launch(dispatcher) {
                updateProfileUseCase(
                    Profile(
                        id = value.userId,
                        email = value.email,
                        username = value.username,
                        photoUrl = value.photoUrl,
                        preferences = Profile.LearningPreferences(value.field.name, value.level.name, value.goal),
                        learningStyle = value.learningStyle!!,
                        createdAt = System.currentTimeMillis(),
                        lastUpdated = System.currentTimeMillis()
                    )
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
    private fun displayProfileCreationForm(form: ProfileCreationForm) = _state.update {
        when (form) {
            ProfileCreationForm.PersonalInfo -> it.copy(currentForm = ProfileCreationForm.PersonalInfo)
            ProfileCreationForm.StyleQuestionnaire -> it.copy(currentForm = ProfileCreationForm.StyleQuestionnaire)
        }
    }

    companion object {
        const val QUESTION_COUNT = 5
    }
}

