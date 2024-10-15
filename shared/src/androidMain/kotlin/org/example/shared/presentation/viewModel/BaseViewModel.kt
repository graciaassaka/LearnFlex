package org.example.shared.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.UIEvent


open class BaseViewModel : ViewModel()
{
    private val _uiEvent = MutableSharedFlow<UIEvent>(1)
    val uiEvent = _uiEvent.asSharedFlow()

    private val _isScreenVisible = MutableStateFlow(true)
    val isScreenVisible = _isScreenVisible.asStateFlow()

    private var navDestination: Route? = null

    fun navigate(destination: Route, waitForAnimation: Boolean)
    {
        if (waitForAnimation)
        {
            navDestination = destination
            _isScreenVisible.update { false }
        } else
        {
            viewModelScope.launch {
                _uiEvent.emit(UIEvent.Navigate(destination))
            }
        }
    }

    fun onExitAnimationFinished() = navDestination?.let {
        viewModelScope.launch {
            _uiEvent.emit(UIEvent.Navigate(it))
            navDestination = null
        }
    }

    fun handleError(error: Throwable)
    {
        showSnackbar(error.message ?: "An error occurred", SnackbarType.Error)
    }

    fun showSnackbar(message: String, type: SnackbarType) = viewModelScope.launch {
        _uiEvent.emit(UIEvent.ShowSnackbar(message, type))
    }
}