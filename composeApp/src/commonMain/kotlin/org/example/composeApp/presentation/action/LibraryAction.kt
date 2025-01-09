package org.example.composeApp.presentation.action

import org.example.composeApp.presentation.navigation.Route
import org.example.shared.domain.model.Curriculum
import java.io.File

/**
 * Represents actions that can be performed in the library.
 */
sealed class LibraryAction {

    /**
     * Action to refresh the data object.
     */
    data object Refresh : LibraryAction()

    /**
     * Action to generate a summary of the syllabus from a provided file.
     * @property file The file containing the syllabus to be summarized.
     */
    data class SummarizeSyllabus(val file: File) : LibraryAction()

    /**
     * Action to open the syllabus.
     */
    data object DeleteSyllabusFile : LibraryAction()

    /**
     * Action to edit the description of a syllabus.
     * @param description The new description for the syllabus.
     */
    data class EditSyllabusDescription(val description: String) : LibraryAction()

    /**
     * Action to generate content.
     */
    data object GenerateCurriculum : LibraryAction()

    /**
     * Action to cancel the generation of a curriculum.
     */
    data object CancelGeneration : LibraryAction()

    /**
     * Action to generate a module.
     * @param title The title of the module to generate.
     */
    data class GenerateModule(val title: String) : LibraryAction()

    /**
     * Action to remove a module.
     * @param title The title of the module to remove.
     */
    data class RemoveModule(val title: String) : LibraryAction()

    /**
     * Action to remove a lesson from a module.
     * @param lessonTitle The title of the lesson to remove.
     * @param moduleId The ID of the module containing the lesson.
     */
    data class RemoveLesson(val lessonTitle: String, val moduleId: String) : LibraryAction()

    /**
     * Action to save content.
     */
    data object SaveContent : LibraryAction()

    /**
     * Action to discard content.
     */
    data object DiscardContent : LibraryAction()

    /**
     * Action to hide the discard warning dialog.
     */
    data object HideDiscardWarningDialog : LibraryAction()

    /**
     * Action to edit the search query.
     * @param query The new search query.
     */
    data class EditFilterQuery(val query: String) : LibraryAction()

    /**
     * Action to clear the search query in the library.
     */
    data object ClearFilterQuery : LibraryAction()

    /**
     * Action to open a specific curriculum.
     * @param curriculumId The ID of the curriculum to be opened.
     */
    data class OpenCurriculum(val curriculumId: String) : LibraryAction()

    /**
     * Action to delete a curriculum.
     * @param curriculum The curriculum to delete.
     */
    data class DeleteCurriculum(val curriculum: Curriculum) : LibraryAction()

    /**
     * Action to handle an error.
     * @param error The error to handle.
     */
    data class HandleError(val error: Throwable) : LibraryAction()

    /**
     * Action to handle navigation.
     * @param destination The destination to navigate to.
     */
    data class Navigate(val destination: Route) : LibraryAction()
}