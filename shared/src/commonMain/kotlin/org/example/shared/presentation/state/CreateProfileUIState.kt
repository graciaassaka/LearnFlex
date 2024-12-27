package org.example.shared.presentation.state

import org.example.shared.domain.client.StyleQuizGeneratorClient
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile
import org.example.shared.presentation.util.ProfileCreationForm

/**
 * Represents the UI state for the Create Profile screen.
 *
 * @property currentForm The current form being displayed in the profile creation process.
 * @property userId The ID of the user creating the profile.
 * @property username The username input by the user.
 * @property usernameError An error message related to the username input, if any.
 * @property email The email of the user.
 * @property photoUrl The URL of the user's profile photo.
 * @property field The selected field of learning for the user.
 * @property level The selected level of learning for the user.
 * @property isLevelDropdownVisible Indicates whether the level dropdown is visible.
 * @property goal The goal input by the user.
 * @property isProfileCreated Indicates whether the profile has been successfully created.
 * @property styleQuestionnaire The style questionnaire containing a list of style questions.
 * @property styleResponses The list of styles selected by the user in the questionnaire.
 * @property learningStyle The result of the style questionnaire.
 * @property showStyleResultDialog Indicates whether to show the style result dialog.
 * @property isLoading Indicates whether the screen is currently in a loading state.
 */
data class CreateProfileUIState(
    val currentForm: ProfileCreationForm = ProfileCreationForm.PERSONAL_INFO,
    val userId : String = "",
    val username: String = "",
    val usernameError: String? = null,
    val email: String = "",
    val photoUrl: String = "",
    val field: Field = Field.entries.first(),
    val level: Level = Level.entries.first(),
    val isLevelDropdownVisible: Boolean = false,
    val goal: String = "",
    val isProfileCreated: Boolean = false,
    val styleQuestionnaire: List<StyleQuizGeneratorClient.StyleQuestion> = emptyList(),
    val styleResponses: List<Style> = emptyList(),
    val learningStyle: Profile.LearningStyle? = null,
    val showStyleResultDialog: Boolean = false,
    val isLoading: Boolean = false
)