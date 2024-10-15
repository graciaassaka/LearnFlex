package org.example.shared.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.state.SharedState

/**
 * ViewModel class for managing shared state and user data.
 *
 * @property getUserDataUseCase The use case for fetching user data.
 * @property dispatcher The coroutine dispatcher for executing tasks.
 * @property [org.example.shared.presentation.state.SharedState] The strategy for starting and stopping the sharing of the state.
 */
actual class SharedViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
) : ViewModel()
{
    // MutableStateFlow to hold the current state of the ViewModel.
    private val _state = MutableStateFlow(SharedState())

    // StateFlow to expose the current state as an immutable flow.
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    /**
     * Fetches user data and updates the state accordingly.
     */
    actual suspend fun getUserData() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch {
            withContext(dispatcher) { getUserDataUseCase() }
                .onSuccess { userData -> update { it.copy(userData = userData) } }
                .onFailure { error -> update { it.copy(errorMessage = error.message) } }
        }

        update { it.copy(isLoading = false) }
    }

    /**
     * Clears any error message in the state.
     */
    actual fun clearError() = _state.update { it.copy(errorMessage = null) }
}