package org.example.composeApp.presentation.state

import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import java.time.DayOfWeek

/**
 * UI state for the dashboard screen.
 *
 * @property profileId User profile ID.
 * @property isLoading Flag indicating if the data is being loaded.
 * @property weeklyActivity Weekly activity data.
 * @property totalMinutes Total minutes spent on the platform.
 * @property averageMinutes Average minutes spent on the platform.
 * @property curricula List of curricula.
 * @property activeCurriculum Active curriculum.
 * @property modules List of modules.
 * @property moduleCountByStatus Module count by status.
 * @property lessonCountByStatus Lesson count by status.
 * @property sectionCountByStatus Section count by status.
 * @property itemsCompletion Items completion data.
 */
data class DashboardUIState(
    val profileId: String = "",
    val isLoading: Boolean = false,
    val weeklyActivity: Map<DayOfWeek, Pair<Long, Int>> = emptyMap(),
    val totalMinutes: Int = 0,
    val averageMinutes: Int = 0,
    val curricula: List<Curriculum> = emptyList(),
    val activeCurriculum: Curriculum? = null,
    val modules: List<Module> = emptyList(),
    val moduleCountByStatus: Map<Status, Int> = emptyMap(),
    val lessonCountByStatus: Map<Status, Int> = emptyMap(),
    val sectionCountByStatus: Map<Status, Int> = emptyMap(),
    val itemsCompletion: List<Triple<String, Int, Int>> = emptyList()
)