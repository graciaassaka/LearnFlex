package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.example.composeApp.presentation.action.DashboardAction
import org.example.composeApp.presentation.state.DashboardUIState
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.FetchWeeklyActivityByUserUseCase
import org.example.shared.domain.use_case.curriculum.FetchActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculumBundleUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase

/**
 * ViewModel for the dashboard screen.
 *
 * @property fetchProfileUseCase The use case to fetch the profile.
 * @property fetchWeeklyActivityByUserUseCase The use case to fetch the weekly activity by user.
 * @property fetchActiveCurriculumUseCase The use case to fetch the active curriculum.
 * @property fetchCurriculumBundleUseCase The use case to fetch the curriculum bundle.
 * @property countModulesByStatusUseCase The use case to count the modules by status.
 * @property countLessonsByStatusUseCase The use case to count the lessons by status.
 * @property countSectionsByStatusUseCase The use case to count the sections by status.
 * @property dispatcher The coroutine dispatcher to run the code on.
 * @property savedStateHandle The handle to save and retrieve UI-related data.
 * @property syncManagers The list of sync managers to handle syncing of data.
 * @property sharingStarted The strategy to start sharing the state flow.
 */
class DashboardViewModel(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val fetchWeeklyActivityByUserUseCase: FetchWeeklyActivityByUserUseCase,
    private val fetchActiveCurriculumUseCase: FetchActiveCurriculumUseCase,
    private val fetchCurriculumBundleUseCase: FetchCurriculumBundleUseCase,
    private val countModulesByStatusUseCase: CountModulesByStatusUseCase,
    private val countLessonsByStatusUseCase: CountLessonsByStatusUseCase,
    private val countSectionsByStatusUseCase: CountSectionsByStatusUseCase,
    private val dispatcher: CoroutineDispatcher,
    private val savedStateHandle: SavedStateHandle,
    syncManagers: List<SyncManager<DatabaseRecord>>,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher, syncManagers) {

    private val _state = MutableStateFlow(DashboardUIState())
    val state = _state
        .onStart { refresh() }
        .stateIn(viewModelScope, sharingStarted, _state.value)

    /**
     * Handles various actions related to the dashboard by invoking the appropriate use cases or methods.
     *
     * @param action The specific action to handle, represented as a sealed class of type [DashboardAction].
     */
    fun handleAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.Refresh -> refresh()
            is DashboardAction.OpenCurriculum -> TODO()
            is DashboardAction.OpenModule -> TODO()
            is DashboardAction.Navigate -> navigate(action.destination)
        }
    }

    /**
     * Initiates the entire data fetching process for the dashboard.
     * This function is non-suspending and safely launches a coroutine.
     */
    private fun refresh() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch(dispatcher) {
            try {
                _state.update { it.copy(profileId = savedStateHandle["userId"] ?: fetchProfileUseCase().getOrThrow().id) }
                listOf(
                    launch { fetchWeeklyActivity() },
                    launch { fetchActiveCurriculumData() }
                ).joinAll()
                updateItemsCompletion()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }


    /**
     * Updates the weekly activity data in the state.
     */
    private suspend fun fetchWeeklyActivity() {
        try {
            val activity = fetchWeeklyActivityByUserUseCase(_state.value.profileId, System.currentTimeMillis()).getOrThrow()
            _state.update { current ->
                with(activity) {
                    current.copy(
                        weeklyActivity = this,
                        totalMinutes = values.sumOf { it.first.toInt() },
                        averageMinutes = if (isNotEmpty()) values.sumOf { it.first.toInt() } / size else 0
                    )
                }
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Fetches the data for the active curriculum.
     */
    private suspend fun fetchActiveCurriculumData() = supervisorScope {
        try {
            val profileId = _state.value.profileId
            val curriculum = fetchActiveCurriculumUseCase(profileId).getOrThrow()
            if (curriculum != null) {
                val bundle = fetchCurriculumBundleUseCase(profileId, curriculum)
                val moduleJob = async { countModulesByStatusUseCase(curriculum.id).getOrThrow() }
                val lessonJob = async { countLessonsByStatusUseCase(curriculum.id).getOrThrow() }
                val sectionJob = async { countSectionsByStatusUseCase(curriculum.id).getOrThrow() }

                _state.update {
                    it.copy(
                        activeCurriculum = curriculum,
                        modules = bundle.modules,
                        moduleCountByStatus = moduleJob.await(),
                        lessonCountByStatus = lessonJob.await(),
                        sectionCountByStatus = sectionJob.await()
                    )
                }
            }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    /**
     * Updates the completion data for the items in the active curriculum.
     */
    private fun updateItemsCompletion() = with(_state) {
        update { current ->
            current.copy(
                itemsCompletion = listOf(
                    Triple(
                        first = Collection.MODULES.value,
                        second = value.moduleCountByStatus[Status.FINISHED] ?: 0,
                        third = value.moduleCountByStatus.values.sum()
                    ),
                    Triple(
                        first = Collection.LESSONS.value,
                        second = value.lessonCountByStatus[Status.FINISHED] ?: 0,
                        third = value.lessonCountByStatus.values.sum()
                    ),
                    Triple(
                        first = Collection.SECTIONS.value,
                        second = value.sectionCountByStatus[Status.FINISHED] ?: 0,
                        third = value.sectionCountByStatus.values.sum()
                    )
                )
            )
        }
    }
}
