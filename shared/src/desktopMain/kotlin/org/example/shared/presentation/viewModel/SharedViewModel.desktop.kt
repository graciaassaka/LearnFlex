package org.example.shared.presentation.viewModel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.state.SharedState

/**
 * ViewModel class for managing shared state and user data on the desktop platform.
 *
 * @property getUserDataUseCase The use case for fetching user data.
 * @property dispatcher The coroutine dispatcher for executing tasks.
 * @property sharingStarted The strategy for starting and stopping the sharing of the state.
 */
actual class SharedViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
) {
    // Coroutine scope tied to the ViewModel's lifecycle.
    val viewModelScope = CoroutineScope(SupervisorJob() + dispatcher)

    // MutableStateFlow to hold the current state of the ViewModel.
    private val _state = MutableStateFlow(SharedState())

    // StateFlow to expose the current state as an immutable flow.
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    /**
     * Fetches user data and updates the state accordingly.
     */
    actual suspend fun getUserData() {
        viewModelScope.launch {
            withContext(dispatcher) { getUserDataUseCase() }
                .onSuccess { userData -> _state.update { it.copy(userData = userData) } }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.message) } }
        }
    }

    /**
     * Clears any error message in the state.
     */
    actual fun clearError() = _state.update { it.copy(errorMessage = null) }

    /**
     * Cancels the ViewModel's coroutine scope when the ViewModel is cleared.
     */
    fun onCleared() = viewModelScope.cancel()
}