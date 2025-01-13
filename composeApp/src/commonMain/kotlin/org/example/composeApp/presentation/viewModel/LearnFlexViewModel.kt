package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.composeApp.presentation.action.LearnFlexAction
import org.example.composeApp.presentation.state.AppState
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.model.util.SessionManager
import org.example.shared.domain.use_case.curriculum.FetchCurriculaByUserUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculumBundleUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.session.FetchSessionsByUserUseCase

/**
 * ViewModel for handling actions and state related to learning flex.
 *
 * @property dispatcher CoroutineDispatcher for managing coroutine context.
 * @property fetchCurriculaByUserUseCase Use case for fetching curricula by user.
 * @property fetchCurriculumBundleUseCase Use case for fetching curriculum bundles.
 * @property fetchProfileUseCase Use case for fetching user profile.
 * @property fetchSessionsByUserUseCase Use case for fetching sessions by user.
 */
class LearnFlexViewModel(
    private val dispatcher: CoroutineDispatcher,
    private val fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase,
    private val fetchCurriculumBundleUseCase: FetchCurriculumBundleUseCase,
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val fetchSessionsByUserUseCase: FetchSessionsByUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    /**
     * Handles actions related to learning flex.
     *
     * @param action The action to handle.
     */
    fun handleAction(action: LearnFlexAction) = when (action) {
        is LearnFlexAction.Refresh -> refresh()
    }


    /**
     * Refreshes the state by fetching profile, sessions, curricula, and bundles.
     */
    private fun refresh() = viewModelScope.launch(dispatcher) {
        try {
            _state.update { it.copy(isLoading = true) }
            val profile = fetchProfileUseCase().getOrThrow()
            val sessions = async { fetchSessionsByUserUseCase(profile.id).getOrThrow() }
            val curricula = async { fetchCurriculaByUserUseCase(profile.id).getOrThrow() }
            val bundles = curricula.await().map { async { fetchCurriculumBundleUseCase(profile.id, it) } }

            _state.update {
                it.copy(
                    profile = profile,
                    sessionManager = SessionManager(sessions.await()),
                    bundleManager = BundleManager(bundles.map { it.await() })
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = e) }
        } finally {
            _state.update { it.copy(isLoading = false) }
        }
    }
}