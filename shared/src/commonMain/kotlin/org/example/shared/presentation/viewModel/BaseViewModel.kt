package org.example.shared.presentation.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.shared.presentation.navigation.Route
import org.example.shared.presentation.util.SnackbarType
import org.example.shared.presentation.util.UIEvent

/**
 * Base ViewModel class providing common functionality for all ViewModels.
 */
open class BaseViewModel(
    private val dispatcher: CoroutineDispatcher
) : ViewModel() {
    // SharedFlow for emitting UI events.
    private val _uiEvent = MutableSharedFlow<UIEvent>(1)
    val uiEvent = _uiEvent.asSharedFlow()

    // StateFlow to track the visibility of the screen.
    private val _isScreenVisible = MutableStateFlow(true)
    val isScreenVisible = _isScreenVisible.asStateFlow()

    // Variable to store the navigation destination.
    private var navDestination: Route? = null

    /**
     * Navigates to the specified destination.
     *
     * @param destination The route to navigate to.
     * @param waitForAnimation Whether to wait for the exit animation to finish before navigating.
     */
    fun navigate(destination: Route, waitForAnimation: Boolean) {
        if (waitForAnimation) {
            navDestination = destination
            _isScreenVisible.update { false }
        } else {
            viewModelScope.launch(dispatcher) {
                _uiEvent.emit(UIEvent.Navigate(destination))
            }
        }
    }

    /**
     * Called when the exit animation is finished.
     * Navigates to the stored destination if available.
     */
    fun onExitAnimationFinished() = navDestination?.let {
        viewModelScope.launch(dispatcher) {
            _uiEvent.emit(UIEvent.Navigate(it))
            navDestination = null
        }
    }

    /**
     * Handles errors by showing a snackbar with the error message.
     *
     * @param error The error to handle.
     */
    fun handleError(error: Throwable) {
        showSnackbar(error.message ?: "An error occurred", SnackbarType.Error)
    }

    /**
     * Shows a snackbar with the specified message and type.
     *
     * @param message The message to display in the snackbar.
     * @param type The type of the snackbar.
     */
    fun showSnackbar(message: String, type: SnackbarType) = viewModelScope.launch(dispatcher) {
        _uiEvent.emit(UIEvent.ShowSnackbar(message, type))
    }
}