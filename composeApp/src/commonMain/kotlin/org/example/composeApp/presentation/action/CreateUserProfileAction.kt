package org.example.composeApp.presentation.action

import org.example.composeApp.presentation.state.CreateProfileUIState.ProfileCreationForm
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style

/**
 * Sealed class representing various actions related to creating a user profile.
 */
sealed class CreateUserProfileAction {

    /**
     * Action to handle the creation of the profile.
     */
    data object CreateProfile : CreateUserProfileAction()

    /**
     * Action to handle deletion of the profile picture.
     */
    data object DeleteProfilePicture : CreateUserProfileAction()

    /**
     * Action to display the profile creation form.
     * @param form The profile creation form to display.
     */
    data class DisplayProfileCreationForm(val form: ProfileCreationForm) : CreateUserProfileAction()

    /**
     * Action to handle changes in the username.
     * @param username The new username.
     */
    data class EditUsername(val username: String) : CreateUserProfileAction()

    /**
     * Action to handle changes in the goal.
     * @param goal The new goal.
     */
    data class EditGoal(val goal: String) : CreateUserProfileAction()

    /**
     * Action to handle an error.
     * @param error The error to handle.
     */
    data class HandleError(val error: Throwable) : CreateUserProfileAction()

    /**
     * Action to handle answering a question in the style questionnaire.
     * @param style The style selected as an answer.
     */
    data class HandleQuestionAnswered(val style: Style) : CreateUserProfileAction()

    /**
     * Action to handle the end of an animation.
     */
    data object HandleAnimationEnd : CreateUserProfileAction()

    /**
     * Action to handle the completion of the questionnaire.
     */
    data object HandleQuestionnaireCompleted : CreateUserProfileAction()

    /**
     * Action to set the learning style.
     */
    data object SetLearningStyle : CreateUserProfileAction()

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
     * Action to start the style questionnaire.
     */
    data object StartStyleQuestionnaire : CreateUserProfileAction()

    /**
     * Action to toggle the visibility of the level dropdown.
     */
    data object ToggleLevelDropdownVisibility : CreateUserProfileAction()

    /**
     * Action to handle uploading a profile picture.
     * @param imageData The image data of the profile picture.
     */
    data class UploadProfilePicture(val imageData: ByteArray) : CreateUserProfileAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UploadProfilePicture

            return imageData.contentEquals(other.imageData)
        }

        override fun hashCode(): Int {
            return imageData.contentHashCode()
        }
    }
}