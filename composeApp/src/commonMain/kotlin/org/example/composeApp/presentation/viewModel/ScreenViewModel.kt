package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.composeApp.injection.DatabaseSyncManagers
import org.example.composeApp.presentation.action.LearnFlexAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.ui.util.SnackbarType
import org.example.composeApp.presentation.ui.util.UIEvent
import org.example.composeApp.presentation.viewModel.util.ResourceProvider
import org.example.shared.data.remote.util.ApiError
import org.example.shared.domain.sync.SyncManager
import org.koin.java.KoinJavaComponent.inject

/**
 * Base ViewModel class providing common functionality for all ViewModels.
 */
open class ScreenViewModel : ViewModel() {
    // Injected dependencies.
    protected val learnFlexViewModel: LearnFlexViewModel by inject(LearnFlexViewModel::class.java)
    protected val dispatcher: CoroutineDispatcher by inject(CoroutineDispatcher::class.java)
    protected val resourceProvider: ResourceProvider by inject(ResourceProvider::class.java)
    private val syncManagers: DatabaseSyncManagers by inject(DatabaseSyncManagers::class.java)

    // SharedFlow for emitting UI events.
    private val _uiEvent = MutableSharedFlow<UIEvent>(1)
    val uiEvent = _uiEvent.asSharedFlow()

    // StateFlow to track the visibility of the screen.
    private val _isScreenVisible = MutableStateFlow(true)
    val isScreenVisible = _isScreenVisible.asStateFlow()

    // Variable to store the navigation destination.
    private var navDestination: Route? = null

    init {
        syncManagers.forEach { syncManager ->
            viewModelScope.launch(dispatcher) {
                syncManager.syncStatus.collect { status ->
                    if (status is SyncManager.SyncStatus.Error) handleError(status.error)
                }
            }
        }
    }

    /**
     * Refreshes the data by triggering the `LearnFlexAction.Refresh` action
     * in the `LearnFlexViewModel`.
     */
    protected fun refresh() = learnFlexViewModel.handleAction(LearnFlexAction.Refresh)

    /**
     * Navigates to the specified destination.
     *
     * @param destination The route to navigate to.
     * @param waitForAnimation Whether to wait for the exit animation to finish before navigating.
     */
    open fun navigate(destination: Route, waitForAnimation: Boolean = false) {
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
    fun handleExitAnimationFinished() = navDestination?.let {
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
    open fun handleError(error: Throwable) {
        val message = when (error) {
            is ApiError -> error.errorContainer?.error?.message ?: error.message
            else -> error.message
        }
        showSnackbar(
            message = message ?: "An error occurred",
            type = SnackbarType.Error
        )
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