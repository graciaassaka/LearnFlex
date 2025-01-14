package org.example.composeApp.presentation.action

import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level

/**
 * Sealed class representing various actions related to editing a user profile.
 */
sealed class EditUserProfileAction {
    /**
     * Represents an action to delete the user's profile while editing their profile.
     */
    data object DeleteProfile : EditUserProfileAction()

    /**
     * Represents an action to delete the user's profile picture while editing their profile.
     */
    data object DeleteProfilePicture : EditUserProfileAction()

    /**
     * Represents an action to edit the user's profile by updating the user's profile information in the database.
     */
    data object EditProfile : EditUserProfileAction()

    /**
     * Represents an action to edit the user's goal in the profile.
     * @property goal The updated goal text provided by the user.
     */
    data class EditGoal(val goal: String) : EditUserProfileAction()

    /**
     * Represents an action to edit the username of the user profile.
     *
     * @property username The new username to update in the profile.
     */
    data class EditUsername(val username: String) : EditUserProfileAction()

    /**
     * Represents an action to handle errors during the editing of a user profile.
     *
     * @param error The throwable instance representing the error that occurred.
     */
    data class HandleError(val error: Throwable) : EditUserProfileAction()

    /**
     * Represents an action to refresh the user profile state.
     */
    data object Refresh : EditUserProfileAction()

    /**
     * Represents an action to select a specific field of learning in the user profile.
     *
     * @property field The selected field of learning.
     */
    data class SelectField(val field: Field) : EditUserProfileAction()

    /**
     * Represents an action to select a specific learning level in the user profile.
     * @param level The selected learning level, represented by the [Level] enum.
     */
    data class SelectLevel(val level: Level) : EditUserProfileAction()

    /**
     * Represents an action for signing out the user within the context of editing the user profile.
     */
    data object SignOut : EditUserProfileAction()

    /**
     * Represents an action to toggle the visibility of the level dropdown menu
     * in the user profile editing interface.
     */
    data object ToggleLevelDropdownVisibility : EditUserProfileAction()

    /**
     * Represents the action of uploading or editing the user's profile picture.
     *
     * @param imageData A byte array containing the image data for the profile picture.
     */
    data class UploadProfilePicture(val imageData: ByteArray) : EditUserProfileAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UploadProfilePicture
            return imageData.contentEquals(other.imageData)
        }

        override fun hashCode() = imageData.contentHashCode()
    }
}