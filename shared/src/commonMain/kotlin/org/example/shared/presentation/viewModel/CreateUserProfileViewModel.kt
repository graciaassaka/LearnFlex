package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.shared.data.model.Field
import org.example.shared.data.model.LearningPreferences
import org.example.shared.data.model.Level
import org.example.shared.data.model.UserProfile
import org.example.shared.domain.use_case.CreateUserProfileUseCase
import org.example.shared.domain.use_case.DeleteProfilePictureUseCase
import org.example.shared.domain.use_case.UploadProfilePictureUseCase
import org.example.shared.presentation.state.CreateProfileUIState
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.validation.InputValidator
import org.example.shared.presentation.util.validation.ValidationResult

/**
 * ViewModel for creating a user profile.
 *
 * @property sharedViewModel The shared ViewModel instance.
 * @property createUserProfileUseCase Use case for creating a user profile.
 * @property uploadProfilePictureUseCase Use case for uploading a profile picture.
 * @property dispatcher Coroutine dispatcher for managing background tasks.
 * @property sharingStarted Defines when the sharing of the state starts.
 */
class CreateUserProfileViewModel(
    private val sharedViewModel: SharedViewModel,
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher)
{
    // Mutable state flow to manage the UI state of the profile creation screen
    private val _state = MutableStateFlow(CreateProfileUIState())
    val state = _state
        .onStart { sharedViewModel.getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    init
    {
        viewModelScope.launch {
            sharedViewModel.state.collect { sharedState ->
                if (sharedState.error != null)
                {
                    handleError(sharedState.error)
                    sharedViewModel.clearError()
                }
                _state.update {
                    it.copy(
                        username = sharedState.userData?.displayName ?: "",
                        email = sharedState.userData?.email ?: "",
                    )
                }
            }
        }
    }

    /**
     * Handles changes to the username input.
     *
     * @param username The new username input.
     */
    fun onUsernameChanged(username: String) = with(InputValidator.validateUsername(username)) {
        when (this@with)
        {
            is ValidationResult.Valid -> _state.update { it.copy(username = value, usernameError = null) }
            is ValidationResult.Invalid -> _state.update { it.copy(username = username, usernameError = message) }
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
    fun toggleLevelDropdownVisibility() = _state.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }

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
                UserProfile(
                    id = sharedViewModel.state.value.userData?.uid ?: "",
                    email = sharedViewModel.state.value.userData?.email ?: "",
                    username = value.username,
                    photoUrl = value.photoUrl,
                    preferences = LearningPreferences(value.field.name, value.level.name, value.goal)
                )
            ).onSuccess {
                showSnackbar(successMessage, SnackbarType.Success)
            }.onFailure { error ->
                handleError(error)
            }
        }

        update { it.copy(isLoading = false) }
    }
}

