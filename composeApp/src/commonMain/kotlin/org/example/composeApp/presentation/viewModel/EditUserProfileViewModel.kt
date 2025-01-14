package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.delete_photo_success
import learnflex.composeapp.generated.resources.delete_profile_success
import learnflex.composeapp.generated.resources.update_photo_success
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.EditUserProfileUIState
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.auth.SignOutUseCase
import org.example.shared.domain.use_case.profile.*
import org.example.shared.domain.use_case.validation.ValidateUsernameUseCase
import org.example.shared.domain.use_case.validation.util.ValidationResult
import org.example.composeApp.presentation.action.EditUserProfileAction as Action

/**
 * ViewModel responsible for managing the user profile editing screen.
 *
 * @property deleteProfileUseCase Use case to delete the user's profile.
 * @property deleteProfilePictureUseCase Use case to delete the user's profile picture.
 * @property signOutUseCase Use case to sign out the user from their account.
 * @property updateProfileUseCase Use case to update the user's profile information.
 * @property uploadProfilePictureUseCase Use case to upload a new profile picture.
 * @property validateUsernameUseCase Use case for validating a given username.
 */
class EditUserProfileViewModel(
    private val deleteProfileUseCase: DeleteProfileUseCase,
    private val deleteProfilePictureUseCase: DeleteProfilePictureUseCase,
    private val fetchProfilePhotoDownloadUrl: FetchProfilePhotoDownloadUrl,
    private val signOutUseCase: SignOutUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
) : ScreenViewModel() {

    private val _state = MutableStateFlow(EditUserProfileUIState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            learnFlexViewModel.state.collect { appState ->
                appState.error?.let(::handleError)
                with(appState.profile) {
                    _state.update {
                        it.copy(
                            profile = this,
                            username = this?.username.orEmpty(),
                            photoUrl = this?.photoUrl.orEmpty(),
                            photoDownloadUrl = this?.photoUrl?.let { fetchProfilePhotoDownloadUrl(it).getOrNull() }.orEmpty(),
                            goal = this?.preferences?.goal.orEmpty(),
                            field = this?.preferences?.field?.let(Field::valueOf) ?: Field.entries.first(),
                            level = this?.preferences?.level?.let(Level::valueOf) ?: Level.entries.first(),
                            isDownloading = appState.isLoading
                        )
                    }
                }
            }
        }
    }

    /**
     * Handles various user profile editing actions by invoking the corresponding private methods based on the action type.
     *
     * @param action The action to be handled by the ViewModel.
     */
    fun handleAction(action: Action) {
        when (action) {
            is Action.DeleteProfile                 -> deleteProfile()
            is Action.DeleteProfilePicture          -> deleteProfilePicture()
            is Action.EditGoal                      -> editGoal(action.goal)
            is Action.EditProfile                   -> editProfile()
            is Action.EditUsername                  -> editUsername(action.username)
            is Action.HandleError                   -> handleError(action.error)
            is Action.Refresh                       -> refresh()
            is Action.SelectField                   -> selectField(action.field)
            is Action.SelectLevel                   -> selectLevel(action.level)
            is Action.SignOut                       -> signOut()
            is Action.ToggleLevelDropdownVisibility -> toggleLevelDropdownVisibility()
            is Action.UploadProfilePicture          -> uploadProfilePicture(action.imageData)
        }
    }

    /**
     * Deletes the current user profile and signs the user out.
     */
    private fun deleteProfile() = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            _state.update { it.copy(isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.delete_profile_success) }

            deleteProfileUseCase(profile).onFailure { throw it }
            signOut()

            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (error: Throwable) {
            handleError(error)
        } finally {
            _state.update { it.copy(isUploading = false) }
        }
    }

    /**
     * Deletes the user's profile picture and updates the UI state accordingly.
     */
    private fun deleteProfilePicture() = viewModelScope.launch(dispatcher) {
        try {
            _state.update { it.copy(isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.delete_photo_success) }

            deleteProfilePictureUseCase().onFailure { throw it }

            _state.update { it.copy(photoUrl = "") }
            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (error: Throwable) {
            handleError(error)
        } finally {
            _state.update { it.copy(isUploading = false) }
        }
    }

    /**
     * Updates the user's goal in the current UI state.
     *
     * @param goal The new goal to be updated in the user's profile.
     */
    private fun editGoal(goal: String) = _state.update { it.copy(goal = goal) }

    /**
     * Updates the user's profile with new information and manages the associated states.
     */
    private fun editProfile() = viewModelScope.launch(dispatcher) {
        try {
            val profile = checkNotNull(_state.value.profile)
            _state.update { it.copy(isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.delete_photo_success) }

            with(_state.value) {
                updateProfileUseCase(
                    profile.copy(
                        username = username,
                        preferences = Profile.LearningPreferences(level.name, field.name, goal),
                        lastUpdated = System.currentTimeMillis()
                    )
                ).onFailure { throw it }
            }

            showSnackbar(successMessage.await(), SnackbarType.Success)
        } catch (error: Throwable) {
            handleError(error)
        } finally {
            _state.update { it.copy(isUploading = false) }
        }
    }

    /**
     * Updates the user's username in the state after validating it.
     * If the username is valid, it updates the state with the new username and clears any error message.
     * If the username is invalid, it retains the provided username and updates the state with the error message.
     *
     * @param username The new username to be set, which will be validated before updating the state.
     */
    private fun editUsername(username: String) = _state.update {
        with(validateUsernameUseCase(username)) {
            when (this@with) {
                is ValidationResult.Valid   -> it.copy(username = value, usernameError = null)
                is ValidationResult.Invalid -> it.copy(username = username, usernameError = message)
            }
        }
    }

    /**
     * Updates the current state with the selected field of learning.
     *
     * @param field The field of learning to be selected and set in the state.
     */
    private fun selectField(field: Field) = _state.update { it.copy(field = field) }

    /**
     * Updates the current state with the selected learning level.
     *
     * @param level The level of learning to be set in the state.
     */
    private fun selectLevel(level: Level) = _state.update { it.copy(level = level) }

    /**
     * Signs out the current user and navigates to the authentication route.
     *
     * This function uses the `signOutUseCase` to perform the sign-out operation and then
     * navigates to the authentication screen. It executes within the `viewModelScope`
     * using a specified dispatcher.
     */
    private fun signOut() = viewModelScope.launch(dispatcher) {
        signOutUseCase()
        navigate(Route.Auth)
    }

    /**
     * Toggles the visibility of the level dropdown in the user interface.
     * Updates the current state to reflect the opposite value of the `isLevelDropdownVisible` property.
     */
    private fun toggleLevelDropdownVisibility() = _state.update { it.copy(isLevelDropdownVisible = !it.isLevelDropdownVisible) }

    /**
     * Handles the process of uploading a new profile picture, updating the UI state,
     * and showing appropriate messages upon success or failure.
     *
     * @param imageData The image data in byte array format to be uploaded.
     */
    private fun uploadProfilePicture(imageData: ByteArray) = viewModelScope.launch(dispatcher) {
        try {
            _state.update { it.copy(isUploading = true) }

            val successMessage = async { resourceProvider.getString(Res.string.update_photo_success) }

            val newUrl = uploadProfilePictureUseCase(imageData).getOrThrow()

            _state.update { it.copy(photoUrl = newUrl) }

            showSnackbar(successMessage.await(), SnackbarType.Success)
            refresh()
        } catch (error: Throwable) {
            handleError(error)
        } finally {
            _state.update { it.copy(isUploading = false) }
        }
    }
}