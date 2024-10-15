package org.example.shared.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.example.shared.domain.use_case.GetUserDataUseCase
import org.example.shared.presentation.state.SharedState

actual class SharedViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted
) : ViewModel()
{
    private val _state = MutableStateFlow(SharedState())
    val state = _state
        .onStart { getUserData() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    actual suspend fun getUserData() = with(_state) {
        update { it.copy(isLoading = true) }

        viewModelScope.launch {
            withContext(dispatcher) { getUserDataUseCase() }
                .onSuccess { userData -> update { it.copy(userData = userData) } }
                .onFailure { error -> update { it.copy(errorMessage = error.message) } }
        }

        update { it.copy(isLoading = false) }
    }

    actual fun clearError() = _state.update { it.copy(errorMessage = null) }
}