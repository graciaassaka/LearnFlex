package org.example.composeApp.presentation.action

import org.example.composeApp.presentation.navigation.Route

/**
 * Represents actions that can be performed on the dashboard.
 */
sealed class DashboardAction {
    /**
     * Represents an action to refresh the dashboard data or state.
     * Intended to trigger updates or reload relevant resources within the dashboard context.
     */
    data object Refresh : DashboardAction()

    /**
     * Represents an action to open a specific curriculum within the dashboard.
     *
     * @param curriculumId The unique ID of the curriculum to be opened.
     */
    data class OpenCurriculum(val curriculumId: String) : DashboardAction()

    /**
     * Represents an action to open a specific module in the dashboard.
     * @param moduleId The ID of the module to be opened.
     */
    data class OpenModule(val moduleId: String) : DashboardAction()

    /**
     * Action to handle navigation.
     * @param destination The destination to navigate to.
     */
    data class Navigate(val destination: Route) : DashboardAction()
}