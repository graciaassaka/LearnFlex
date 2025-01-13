package org.example.composeApp.presentation.viewModel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.composeApp.presentation.action.DashboardAction
import org.example.composeApp.presentation.navigation.Route
import org.example.composeApp.presentation.state.DashboardUIState
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.util.BundleManager
import org.example.shared.domain.model.util.SessionManager
import java.time.DayOfWeek

/**
 * ViewModel for the dashboard screen.
 *
 * @property learnFlexViewModel The `LearnFlexViewModel` instance to access the app state.
 */
class DashboardViewModel(
) : ScreenViewModel() {
    private val _state = MutableStateFlow(DashboardUIState())
    val state = _state.asStateFlow()

    private lateinit var bundleManager: BundleManager
    private lateinit var sessionManager: SessionManager

    init {
        viewModelScope.launch(dispatcher) {
            learnFlexViewModel.state.collect { appState ->
                if (appState.error != null) handleError(appState.error)
                _state.update { it.copy(isLoading = appState.isLoading) }
                bundleManager = appState.bundleManager
                sessionManager = appState.sessionManager
                updateState()
            }
        }
    }

    /**
     * Handles various actions related to the dashboard by invoking the appropriate use cases or methods.
     *
     * @param action The specific action to handle, represented as a sealed class of type [DashboardAction].
     */
    fun handleAction(action: DashboardAction) = when (action) {
        is DashboardAction.Navigate       -> navigate(action.destination)
        is DashboardAction.OpenCurriculum -> navigate(Route.Study(curriculumId = state.value.curriculum?.id))
        is DashboardAction.OpenModule     -> navigate(Route.Study(curriculumId = state.value.curriculum?.id, moduleId = action.moduleId))
        is DashboardAction.Refresh        -> refresh()
    }

    /**
     * Refreshes the data by triggering the `LearnFlexAction.Refresh` action
     * in the `LearnFlexViewModel`.
     */
    private fun updateState() {
        sessionManager.calculateWeeklyActivity(System.currentTimeMillis()).let(::updateActivity)
        bundleManager.getLatestCurriculum()?.let(::updateActiveCurriculumData)
        updateItemsCompletion()
    }

    /**
     * Updates the weekly activity data in the state.
     *
     * @param activity The weekly activity data to update.
     */
    private fun updateActivity(activity: Map<DayOfWeek, Pair<Long, Int>>) {
        if (activity.isNotEmpty()) _state.update {
            it.copy(
                weeklyActivity = activity,
                totalMinutes = activity.values.sumOf { it.first.toInt() },
                averageMinutes = activity.values.sumOf { it.first.toInt() } / activity.size
            )
        }
    }

    /**
     * Updates the active curriculum data in the state.
     *
     * @param curriculum The active curriculum to update.
     */
    private fun updateActiveCurriculumData(curriculum: Curriculum) = _state.update {
        it.copy(
            curriculum = curriculum,
            modules = bundleManager.getModulesByCurriculum(curriculum.id),
            moduleCountByStatus = bundleManager.countCurriculumModulesByStatus(curriculum.id),
            lessonCountByStatus = bundleManager.countCurriculumLessonsByStatus(curriculum.id),
            sectionCountByStatus = bundleManager.countCurriculumSectionsByStatus(curriculum.id)
        )
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
