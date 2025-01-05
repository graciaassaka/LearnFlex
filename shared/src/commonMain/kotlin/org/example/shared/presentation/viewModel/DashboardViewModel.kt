package org.example.shared.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.interfaces.DatabaseRecord
import org.example.shared.domain.sync.SyncManager
import org.example.shared.domain.use_case.activity.GetWeeklyActivityUseCase
import org.example.shared.domain.use_case.curriculum.GetActiveCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.GetAllCurriculaUseCase
import org.example.shared.domain.use_case.lesson.CountLessonsByStatusUseCase
import org.example.shared.domain.use_case.lesson.GetAllLessonsUseCase
import org.example.shared.domain.use_case.module.CountModulesByStatusUseCase
import org.example.shared.domain.use_case.module.GetAllModulesUseCase
import org.example.shared.domain.use_case.path.*
import org.example.shared.domain.use_case.profile.GetProfileUseCase
import org.example.shared.domain.use_case.section.CountSectionsByStatusUseCase
import org.example.shared.domain.use_case.section.GetAllSectionsUseCase
import org.example.shared.domain.use_case.session.GetAllSessionsUseCase
import org.example.shared.presentation.action.DashboardAction
import org.example.shared.presentation.state.DashboardUIState
import java.time.DayOfWeek

/**
 * ViewModel responsible for managing the dashboard data.
 *
 * @property buildProfilePathUseCase Use case to build the profile path.
 * @property buildCurriculumPathUseCase Use case to build the curriculum path.
 * @property buildModulePathUseCase Use case to build the module path.
 * @property buildLessonPathUseCase Use case to build the lesson path.
 * @property buildSectionPathUseCase Use case to build the section path.
 * @property getProfileUseCase Use case to fetch the user profile.
 * @property getActiveCurriculumUseCase Use case to fetch the active curriculum.
 * @property getAllCurriculaUseCase Use case to fetch all curricula.
 * @property getAllLessonsUseCase Use case to fetch all lessons.
 * @property getAllSectionsUseCase Use case to fetch all sections.
 * @property getAllModulesUseCase Use case to fetch all modules.
 * @property getWeeklyActivityUseCase Use case to fetch weekly activity data.
 * @property countModulesByStatusUseCase Use case to count modules by status.
 * @property countLessonsByStatusUseCase Use case to count lessons by status.
 * @property countSectionsByStatusUseCase Use case to count sections by status.
 * @property dispatcher Coroutine dispatcher for executing coroutines.
 * @property syncManagers List of synchronization managers.
 * @property sharingStarted Sharing strategy for the state flow.
 */
class DashboardViewModel(
    private val buildProfilePathUseCase: BuildProfilePathUseCase,
    private val buildSessionPathUseCase: BuildSessionPathUseCase,
    private val buildCurriculumPathUseCase: BuildCurriculumPathUseCase,
    private val buildModulePathUseCase: BuildModulePathUseCase,
    private val buildLessonPathUseCase: BuildLessonPathUseCase,
    private val buildSectionPathUseCase: BuildSectionPathUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val getActiveCurriculumUseCase: GetActiveCurriculumUseCase,
    private val getAllCurriculaUseCase: GetAllCurriculaUseCase,
    private val getAllLessonsUseCase: GetAllLessonsUseCase,
    private val getAllSectionsUseCase: GetAllSectionsUseCase,
    private val getAllModulesUseCase: GetAllModulesUseCase,
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
                val profile = getProfileUseCase(buildProfilePathUseCase()).getOrThrow()
                _state.update { it.copy(profile = profile) }

                getAllSessionsUseCase(buildSessionPathUseCase(profile.id)).getOrThrow()

                val weeklyActivity = getWeeklyActivityUseCase(System.currentTimeMillis()).getOrThrow()
                updateWeeklyActivity(weeklyActivity)

                val curricula = getAllCurriculaUseCase(buildCurriculumPathUseCase(profile.id)).getOrThrow()
                _state.update { it.copy(curricula = curricula) }

                val activeCurriculum = getActiveCurriculumUseCase(buildCurriculumPathUseCase(profile.id)).getOrThrow()
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
        val modules = getAllModulesUseCase(buildModulePathUseCase(profileId, curriculum.id)).getOrThrow()
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
        val lessons = getAllLessonsUseCase(buildLessonPathUseCase(userId, curriculumId, module.id)).getOrThrow()
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
        getAllSectionsUseCase(buildSectionPathUseCase(userId, curriculumId, moduleId, lessonId)).getOrThrow()
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
