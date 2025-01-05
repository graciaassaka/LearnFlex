package org.example.shared.presentation.action

import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.presentation.util.ProfileCreationForm

/**
 * Sealed class representing various actions related to creating a user profile.
 */
sealed class CreateUserProfileAction {

    /**
     * Action to handle changes in the username.
     * @param username The new username.
     */
    data class EditUsername(val username: String) : CreateUserProfileAction()

    /**
     * Action to handle changes in the field.
     * @param field The new field.
     */
    data class SelectField(val field: Field) : CreateUserProfileAction()

    /**
     * Action to handle changes in the level.
     * @param level The new level.
     */
    data class SelectLevel(val level: Level) : CreateUserProfileAction()

    /**
     * Action to toggle the visibility of the level dropdown.
     */
    data object ToggleLevelDropdownVisibility : CreateUserProfileAction()

    /**
     * Action to handle changes in the goal.
     * @param goal The new goal.
     */
    data class EditGoal(val goal: String) : CreateUserProfileAction()

    /**
     * Action to handle uploading a profile picture.
     * @param imageData The image data of the profile picture.
     * @param successMessage The success message to display.
     */
    data class UploadProfilePicture(val imageData: ByteArray, val successMessage: String) : CreateUserProfileAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UploadProfilePicture

            if (!imageData.contentEquals(other.imageData)) return false
            if (successMessage != other.successMessage) return false

            return true
        }

        override fun hashCode(): Int {
            var result = imageData.contentHashCode()
            result = 31 * result + successMessage.hashCode()
            return result
        }
    }

    /**
     * Action to handle deletion of the profile picture.
     * @param successMessage The success message to display.
     */
    data class DeleteProfilePicture(val successMessage: String) : CreateUserProfileAction()

    /**
     * Action to handle the creation of the profile.
     * @param successMessage The success message to display.
     */
    data class CreateProfile(val successMessage: String) : CreateUserProfileAction()

    /**
     * Action to start the style questionnaire.
     */
    data object StartStyleQuestionnaire : CreateUserProfileAction()

    /**
     * Action to handle answering a question in the style questionnaire.
     * @param style The style selected as an answer.
     */
    data class HandleQuestionAnswered(val style: Style) : CreateUserProfileAction()

    /**
     * Action to handle the completion of the questionnaire.
     */
    data object HandleQuestionnaireCompleted : CreateUserProfileAction()

    /**
     * Action to set the learning style.
     * @param successMessage The success message to display.
     */
    data class SetLearningStyle(val successMessage: String) : CreateUserProfileAction()

    /**
     * Action to display the profile creation form.
     * @param form The profile creation form to display.
     */
    data class DisplayProfileCreationForm(val form: ProfileCreationForm) : CreateUserProfileAction()

    /**
     * Action to handle an error.
     * @param error The error to handle.
     */
    data class HandleError(val error: Throwable) : CreateUserProfileAction()

    /**
     * Action to handle the end of an animation.
     */
    data object HandleAnimationEnd : CreateUserProfileAction()
}