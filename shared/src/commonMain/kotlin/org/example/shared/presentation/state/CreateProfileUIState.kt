package org.example.shared.presentation.state

import org.example.shared.data.model.Field
import org.example.shared.data.model.Level

data class CreateProfileUIState(
    val username: String = "",
    val usernameError: String? = null,
    val email: String = "",
    val photoUrl: String = "",
    val field: Field = Field.entries.first(),
    val level: Level = Level.entries.first(),
    val isLevelDropdownVisible: Boolean = false,
    val goal: String = "",
    val isLoading: Boolean = false,
)