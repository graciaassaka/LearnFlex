package org.example.composeApp.presentation.action

import org.example.composeApp.presentation.navigation.Route

/**
 * Represents various actions that can be performed in the study module.
 */
sealed class StudyAction {
    /**
     * Action to answer a quiz question.
     * @param answer The answer provided.
     */
    data class AnswerQuizQuestion(val answer: Any) : StudyAction()

    /**
     * Action to cancel the current generation process.
     */
    data object CancelGeneration : StudyAction()

    /**
     * Action to generate a lesson.
     * @param title The title of the lesson.
     */
    data class GenerateLesson(val title: String) : StudyAction()

    /**
     * Action to generate a lesson quiz.
     */
    data object GenerateLessonQuiz : StudyAction()

    /**
     * Action to generate a section.
     * @param title The title of the section.
     */
    data class GenerateSection(val title: String) : StudyAction()

    /**
     * Action to generate a section quiz.
     * @param id The ID of the section.
     */
    data class GenerateSectionQuiz(val id: String) : StudyAction()

    /**
     * Action to generate a module quiz.
     */
    data object GenerateModuleQuiz : StudyAction()

    /**
     * Action to navigate to a specific route.
     * @param route The destination route.
     */
    data class Navigate(val route: Route) : StudyAction()

    /**
     * Action to go back to the previous screen.
     */
    data object GoBack : StudyAction()

    /**
     * Action to refresh the current state.
     */
    data object Refresh : StudyAction()

    /**
     * Action to regenerate a lesson.
     * @param id The ID of the lesson.
     */
    data class RegenerateLesson(val id: String) : StudyAction()

    /**
     * Action to regenerate a section.
     * @param id The ID of the section.
     */
    data class RegenerateSection(val id: String) : StudyAction()

    /**
     * Action to save the quiz result.
     */
    data object SaveQuizResult : StudyAction()

    /**
     * Action to select a curriculum.
     * @param curriculumId The ID of the curriculum.
     */
    data class SelectCurriculum(val curriculumId: String) : StudyAction()

    /**
     * Action to select a lesson.
     * @param lessonId The ID of the lesson.
     */
    data class SelectLesson(val lessonId: String) : StudyAction()

    /**
     * Action to select a module.
     * @param moduleId The ID of the module.
     */
    data class SelectModule(val moduleId: String) : StudyAction()

    /**
     * Action to submit the quiz.
     */
    data object SubmitQuiz : StudyAction()
}