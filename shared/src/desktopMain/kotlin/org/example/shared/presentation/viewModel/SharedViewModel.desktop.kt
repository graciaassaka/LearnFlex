package org.example.shared.presentation.viewModel

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.state.SharedState

actual class SharedViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
)
{
    val viewModelScope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(SharedState())
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    actual suspend fun getUserData()
    {
        viewModelScope.launch {
            withContext(dispatcher){ getUserDataUseCase() }
                .onSuccess { userData -> _state.update { it.copy(userData = userData) } }
                .onFailure { error -> _state.update { it.copy(errorMessage = error.message) } }
        }
    }

    actual fun clearError() = _state.update { it.copy(errorMessage = null) }

    fun onCleared() = viewModelScope.cancel()
}