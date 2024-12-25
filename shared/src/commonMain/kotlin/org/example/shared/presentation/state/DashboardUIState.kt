package org.example.shared.presentation.state

import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import java.time.DayOfWeek

/**
 * UI state for the Dashboard screen.
 *
 * @property profile Profile of the user.
 * @property isLoading True if data is being loaded.
 * @property weeklyActivity Weekly activity of the user.
 * @property curricula List of all curricula.
 * @property activeCurriculum Active curriculum of the user.
 * @property modules List of all modules.
 * @property moduleCountByStatus Count of modules by status.
 * @property lessonCountByStatus Count of lessons by status.
 * @property sectionCountByStatus Count of sections by status.
 */
data class DashboardUIState(
    val profile: Profile? = null,
    val isLoading: Boolean = false,
    val weeklyActivity: Map<DayOfWeek, Pair<Long, Int>> = emptyMap(),
    val curricula: List<Curriculum> = emptyList(),
    val activeCurriculum: Curriculum? = null,
    val modules: List<Module> = emptyList(),
    val moduleCountByStatus: Map<Status, Int> = emptyMap(),
    val lessonCountByStatus: Map<Status, Int> = emptyMap(),
    val sectionCountByStatus: Map<Status, Int> = emptyMap(),
)