package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import learnflex.composeapp.generated.resources.*
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.CreateProfileUIState
import org.example.composeApp.presentation.state.CreateProfileUIState.ProfileCreationForm
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.auth.GetUserDataUseCase
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.example.composeApp.presentation.action.CreateUserProfileAction as Action

/**
 * ViewModel for the profile creation screen.
 *
 * @param getUserDataUseCase Use case to get the user data.
 * @param createProfileUseCase Use case to create a user profile.
 * @param uploadProfilePictureUseCase Use case to upload a profile picture.
 * @param deleteProfilePictureUseCase Use case to delete a profile picture.
 * @param fetchStyleQuestionsUseCase Use case to fetch style questions.
 * @param getStyleResultUseCase Use case to get the style result.
 * @param updateProfileUseCase Use case to update a user profile.
 * @param validateUsernameUseCase Use case to validate a username.
 * @param sharingStarted The sharing strategy for the UI state.
 */
class CreateUserProfileViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val createProfileUseCase: CreateProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val fetchStyleQuestionsUseCase: FetchStyleQuestionsUseCase,
    private val getStyleResultUseCase: GetStyleResultUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    sharingStarted: SharingStarted
) : ScreenViewModel() {
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
    fun handleAction(action: Action) = when (action) {
        is Action.CreateProfile                 -> createProfile()
        is Action.DeleteProfilePicture -> deleteProfilePicture()
        is Action.DisplayProfileCreationForm    -> displayProfileCreationForm(action.form)
        is Action.EditGoal                      -> editGoal(action.goal)
        is Action.EditUsername                  -> editUsername(action.username)
        is Action.HandleAnimationEnd            -> handleExitAnimationFinished()
        is Action.HandleError                   -> handleError(action.error)
        is Action.HandleQuestionAnswered        -> handleQuestionAnswered(action.style)
        is Action.HandleQuestionnaireCompleted  -> handleQuestionnaireCompleted()
        is Action.SelectField                   -> selectField(action.field)
        is Action.SelectLevel          -> selectLevel(action.level)
        is Action.SetLearningStyle              -> setLearningStyle()
        is Action.StartStyleQuestionnaire       -> startStyleQuestionnaire()
        is Action.ToggleLevelDropdownVisibility -> toggleLevelDropdownVisibility()
        is Action.UploadProfilePicture          -> uploadProfilePicture(action.imageData)
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
    private fun selectLevel(level: Level) = _state.update { it.copy(level = level) }

    /**
     * Handles changes to the learning level dropdown visibility.
     */
    private fun toggleLevelDropdownVisibility() = _state.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }

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
     */
    private fun uploadProfilePicture(imageData: ByteArray) = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            val successMessage = async { resourceProvider.getString(Res.string.update_photo_success) }
            uploadProfilePictureUseCase(imageData)
                .onSuccess { url ->
                    update { it.copy(photoUrl = url) }
                    showSnackbar(successMessage.await(), SnackbarType.Success)
                }.onFailure { error ->
                    handleError(error)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles the deletion of a profile picture.
     *
     */
    private fun deleteProfilePicture() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            val successMessage = async { resourceProvider.getString(Res.string.delete_photo_success) }
            deleteProfilePictureUseCase()
                .onSuccess {
                    update { it.copy(photoUrl = "") }
                    showSnackbar(successMessage.await(), SnackbarType.Success)
                }.onFailure { error ->
                    handleError(error)
                }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Handles the creation of a user profile.
     */
    private fun createProfile() = with(_state) {
        viewModelScope.launch(dispatcher) {
            try {
                editUsername(value.username)
                check(value.usernameError.isNullOrBlank())

                update { it.copy(isLoading = true) }
                val successMessage = async { resourceProvider.getString(Res.string.create_profile_success) }
                createProfileUseCase(
                    Profile(
                        id = value.userId,
                        email = value.email,
                        username = value.username,
                        photoUrl = value.photoUrl,
                        preferences = Profile.LearningPreferences(value.field, value.level, value.goal),
                    )
                ).getOrThrow()
                showSnackbar(successMessage.await(), SnackbarType.Success)
                update { it.copy(isProfileCreated = true) }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                update { it.copy(isLoading = false) }
            }
        }
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
            fetchStyleQuestionsUseCase(Profile.LearningPreferences(value.field.name, value.level.name, value.goal)).collect { result ->
                result.fold(
                    onSuccess = { question -> update { it.copy(styleQuestionnaire = it.styleQuestionnaire + question) } },
                    onFailure = ::handleError
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
     */
    private fun setLearningStyle() = with(_state) {
        viewModelScope.launch(dispatcher) {
            try {
                check(value.learningStyle != null)
                update { it.copy(isLoading = true) }
                val successMessage = async { resourceProvider.getString(Res.string.set_learning_style_success) }
                updateProfileUseCase(
                    Profile(
                        id = value.userId,
                        email = value.email,
                        username = value.username,
                        photoUrl = value.photoUrl,
                        preferences = Profile.LearningPreferences(value.field, value.level, value.goal),
                        learningStyle = value.learningStyle!!,
                        lastUpdated = System.currentTimeMillis()
                    )
                ).getOrThrow()
                refresh()
                showSnackbar(successMessage.await(), SnackbarType.Success)
                navigate(Route.Dashboard, true)
            } catch (e: Exception) {
                handleError(e)
            } finally {
                update { it.copy(showStyleResultDialog = false, isLoading = false) }
            }
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
}

