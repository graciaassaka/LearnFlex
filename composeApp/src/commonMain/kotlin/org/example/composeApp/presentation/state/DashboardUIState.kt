package org.example.composeApp.presentation.state

import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import java.time.DayOfWeek

/**
 * Represents the state of the dashboard UI.
 *
 * @property profileId The ID of the user's profile.
 * @property isLoading Indicates if the dashboard is currently loading.
 * @property weeklyActivity The user's weekly activity.
 * @property totalMinutes The total number of minutes spent on the platform.
 * @property averageMinutes The average number of minutes spent on the platform.
 * @property curriculum The user's curriculum.
 * @property modules The user's modules.
 * @property moduleCountByStatus The count of modules by status.
 * @property lessonCountByStatus The count of lessons by status.
 * @property sectionCountByStatus The count of sections by status.
 * @property itemsCompletion The completion status of items.
 */
data class DashboardUIState(
    val profileId: String = "",
    val isLoading: Boolean = false,
    val weeklyActivity: Map<DayOfWeek, Pair<Long, Int>> = emptyMap(),
    val totalMinutes: Int = 0,
    val averageMinutes: Int = 0,
    val curriculum: Curriculum? = null,
    val modules: List<Module> = emptyList(),
    val moduleCountByStatus: Map<Status, Int> = emptyMap(),
    val lessonCountByStatus: Map<Status, Int> = emptyMap(),
    val sectionCountByStatus: Map<Status, Int> = emptyMap(),
    val itemsCompletion: List<Triple<String, Int, Int>> = emptyList()
)