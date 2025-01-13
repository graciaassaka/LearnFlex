package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.first
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile
import org.example.shared.domain.use_case.section.DeleteSectionsByLessonUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase

/**
 * Use case for regenerating and updating a lesson. This process involves generating a new lesson,
 * merging its content with an existing lesson, deleting its associated sections, and updating the lesson.
 *
 * @property deleteSections Handles the deletion of associated sections for a lesson.
 * @property fetchSections Retrieves the sections linked to a lesson.
 * @property generateLesson Creates a new lesson using the provided details.
 * @property updateLesson Updates the lesson in the relevant repository.
 */
class RegenerateAndUpdateLessonUseCase(
    private val deleteSections: DeleteSectionsByLessonUseCase,
    private val fetchSections: FetchSectionsByLessonUseCase,
    private val generateLesson: GenerateLessonUseCase,
    private val updateLesson: UpdateLessonUseCase
) {
    /**
     * Regenerates and updates a lesson based on the given parameters.
     *
     * @param profile The profile to which the lesson belongs.
     * @param curriculum The curriculum under which the lesson is categorized.
     * @param module The module under which the lesson is organized.
     * @param lesson The lesson to be regenerated and updated.
     * @return A [Result] wrapping the updated [Lesson], or an exception if the process fails.
     */
    suspend operator fun invoke(
        profile: Profile,
        curriculum: Curriculum,
        module: Module,
        lesson: Lesson
    ) = try {
        val lesson = generateLesson(lesson.title, profile, curriculum, module).first().getOrThrow()
            .run { lesson.copy(title = title, description = description, content = content) }

        val sections = fetchSections(profile.id, curriculum.id, module.id, lesson.id).getOrThrow()
        deleteSections(sections, profile.id, curriculum.id, module.id, lesson.id).getOrThrow()

        updateLesson(lesson, profile.id, curriculum.id, module.id).getOrThrow()

        Result.success(lesson)
    } catch (e: Exception) {
        Result.failure(e)
    }
}