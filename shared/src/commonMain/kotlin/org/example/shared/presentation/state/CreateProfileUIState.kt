package org.example.shared.presentation.state

import org.example.shared.data.model.Field
import org.example.shared.data.model.Level
import org.example.shared.data.util.Style
import org.example.shared.data.model.StyleQuestionnaire
import org.example.shared.data.model.StyleResult
import org.example.shared.presentation.util.ProfileCreationForm

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
    val styleQuestionnaire: StyleQuestionnaire = StyleQuestionnaire(emptyList()),
    val styleResponses: List<Style> = emptyList(),
    val styleResult: StyleResult? = null,
    val showStyleResultDialog: Boolean = false,
    val isLoading: Boolean = false,
)