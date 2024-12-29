package org.example.shared.presentation.action

sealed class DashboardAction {
    data object LoadData : DashboardAction()
    data class OpenCurriculum(val curriculumId: String) : DashboardAction()
    data class OpenModule(val moduleId: String) : DashboardAction()
}