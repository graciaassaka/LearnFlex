package org.example.shared.presentation.state

import org.example.shared.data.model.User

actual data class SharedState(
    val userData: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)