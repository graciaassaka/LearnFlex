package org.example.composeApp.presentation.state

import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.model.Profile

/**
 * UI state representing the state of the user profile editing interface.
 *
 * @param profile The user's profile information.
 * @param username The username of the user.
 * @param usernameError The error message for the username field.
 * @param photoUrl The URL of the user's profile picture.
 * @param photoDownloadUrl The URL of the user's profile picture download.
 * @param field The field of learning selected by the user.
 * @param level The learning level selected by the user.
 * @param isLevelDropdownVisible Flag indicating whether the level dropdown menu is visible.
 * @param goal The user's learning goal.
 * @param isUploading Flag indicating whether the profile picture is being uploaded.
 * @param isDownloading Flag indicating whether the profile picture is being downloaded.
 */
data class EditUserProfileUIState(
    val profile: Profile? = null,
    val username: String = "",
    val usernameError: String? = null,
    val photoUrl: String = "",
    val photoDownloadUrl: String = "",
    val field: Field = Field.entries.first(),
    val level: Level = Level.entries.first(),
    val isLevelDropdownVisible: Boolean = false,
    val goal: String = "",
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
)
