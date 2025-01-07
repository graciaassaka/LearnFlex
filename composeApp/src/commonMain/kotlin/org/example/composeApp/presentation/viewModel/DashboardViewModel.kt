package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.composeApp.presentation.action.DashboardAction
import org.example.composeApp.presentation.state.DashboardUIState
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.GetWeeklyActivityUseCase
import org.example.shared.domain.use_case.curriculum.FetchActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.FetchCurriculaByUserUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.lesson.FetchLessonsByModuleUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.module.FetchModulesByCurriculumUseCase
import org.example.shared.domain.use_case.profile.FetchProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase
import org.example.shared.domain.use_case.session.FetchSessionsByUserUseCase
import java.time.DayOfWeek

/**
 * ViewModel for the dashboard screen.
 *
 * @property fetchProfileUseCase Use case to fetch the user profile.
 * @property fetchSessionsByUserUseCase Use case to fetch user sessions.
 * @property fetchActiveCurriculumUseCase Use case to fetch the active curriculum.
 * @property fetchCurriculaByUserUseCase Use case to fetch curricula by user.
 * @property fetchModulesByCurriculumUseCase Use case to fetch modules by curriculum.
 * @property fetchLessonsByModuleUseCase Use case to fetch lessons by module.
 * @property fetchSectionsByLessonUseCase Use case to fetch sections by lesson.
 * @property getWeeklyActivityUseCase Use case to get weekly activity.
 * @property countModulesByStatusUseCase Use case to count modules by status.
 * @property countLessonsByStatusUseCase Use case to count lessons by status.
 * @property countSectionsByStatusUseCase Use case to count sections by status.
 * @property dispatcher Coroutine dispatcher for background tasks.
 * @property syncManagers List of sync managers for database operations.
 * @property sharingStarted Sharing strategy for state flow.
 */
class DashboardViewModel(
    private val fetchProfileUseCase: FetchProfileUseCase,
    private val fetchSessionsByUserUseCase: FetchSessionsByUserUseCase,
    private val fetchActiveCurriculumUseCase: FetchActiveCurriculumUseCase,
    private val fetchCurriculaByUserUseCase: FetchCurriculaByUserUseCase,
    private val fetchModulesByCurriculumUseCase: FetchModulesByCurriculumUseCase,
    private val fetchLessonsByModuleUseCase: FetchLessonsByModuleUseCase,
    private val fetchSectionsByLessonUseCase: FetchSectionsByLessonUseCase,
    private val getWeeklyActivityUseCase: GetWeeklyActivityUseCase,
    private val countModulesByStatusUseCase: CountModulesByStatusUseCase,
    private val countLessonsByStatusUseCase: CountLessonsByStatusUseCase,
    private val countSectionsByStatusUseCase: CountSectionsByStatusUseCase,
    private val dispatcher: CoroutineDispatcher,
    syncManagers: List<SyncManager<DatabaseRecord>>,
    sharingStarted: SharingStarted
) : BaseViewModel(dispatcher, syncManagers) {

    // Internal mutable state flow to hold the UI state.
    private val _state = MutableStateFlow(DashboardUIState())

    /**
     * Publicly exposed state flow for the UI to observe.
     */
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
            is DashboardAction.OpenCurriculum -> openCurriculum(action.curriculumId)
            is DashboardAction.OpenModule -> openModule(action.moduleId)
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
                val profile = fetchProfileUseCase().getOrThrow()
                _state.update { it.copy(profile = profile) }

                fetchSessionsByUserUseCase(profile.id).getOrThrow()

                val weeklyActivity = getWeeklyActivityUseCase(System.currentTimeMillis()).getOrThrow()
                updateWeeklyActivity(weeklyActivity)

                val curricula = fetchCurriculaByUserUseCase(profile.id).getOrThrow()
                _state.update { it.copy(curricula = curricula) }

                val activeCurriculum = fetchActiveCurriculumUseCase(profile.id).getOrThrow()
                _state.update { it.copy(activeCurriculum = activeCurriculum) }

                if (activeCurriculum != null) fetchActiveCurriculumData(profile.id, activeCurriculum)
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
     *
     * @param weeklyActivity A map where the key is a string representing the day of the week,
     * and the value is a pair of long values representing the activity duration and another metric.
     */
    private fun updateWeeklyActivity(weeklyActivity: Map<DayOfWeek, Pair<Long, Int>>) {
        _state.update { current ->
            with(weeklyActivity) {
                current.copy(
                    weeklyActivity = this,
                    totalMinutes = values.sumOf { it.first.toInt() },
                    averageMinutes = if (isNotEmpty()) values.sumOf { it.first.toInt() } / size else 0
                )
            }
        }
    }

    /**
     * Fetches the data for the active curriculum.
     *
     * @param profileId The ID of the user profile.
     * @param curriculum The active curriculum.
     */
    private suspend fun fetchActiveCurriculumData(profileId: String, curriculum: Curriculum) {
        val modules = fetchModulesByCurriculumUseCase(profileId, curriculum.id).getOrThrow()
        _state.update { it.copy(modules = modules) }
        _state.update { it.copy(moduleCountByStatus = countModulesByStatusUseCase(curriculum.id).getOrThrow()) }
        modules.forEach { module -> fetchLessons(profileId, curriculum.id, module) }
    }

    /**
     * Fetches the lessons for a given module.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param module The module for which to fetch lessons.
     */
    private suspend fun fetchLessons(userId: String, curriculumId: String, module: Module) {
        val lessons = fetchLessonsByModuleUseCase(userId, curriculumId, module.id).getOrThrow()
        _state.update { it.copy(lessonCountByStatus = countLessonsByStatusUseCase(curriculumId).getOrThrow()) }
        lessons.forEach { lesson -> fetchSections(userId, curriculumId, module.id, lesson.id) }
    }

    /**
     * Fetches the sections for a given lesson.
     *
     * @param userId The ID of the user.
     * @param curriculumId The ID of the curriculum.
     * @param moduleId The ID of the module.
     * @param lessonId The ID of the lesson.
     */
    private suspend fun fetchSections(userId: String, curriculumId: String, moduleId: String, lessonId: String) {
        fetchSectionsByLessonUseCase(userId, curriculumId, moduleId, lessonId).getOrThrow()
        _state.update { it.copy(sectionCountByStatus = countSectionsByStatusUseCase(curriculumId).getOrThrow()) }
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

    private fun openModule(moduleId: String) {
        TODO("Navigate to module details screen")
    }

    private fun openCurriculum(curriculumId: String) {
        TODO("Navigate to curriculum details screen")
    }
}
