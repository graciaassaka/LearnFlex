package org.example.shared.domain.use_case.lesson

import kotlinx.coroutines.flow.first
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.model.Module
import org.example.shared.domain.model.Profile

/**
 * Use case for generating and uploading a lesson to the database.
 *
 * @property generateLesson The use case responsible for generating the lesson content.
 * @property uploadLessonUseCase The use case responsible for uploading the lesson to storage.
 */
class GenerateAndUploadLessonUseCase(
    private val generateLesson: GenerateLessonUseCase,
    private val uploadLessonUseCase: UploadLessonUseCase
) {
    /**
     * Generates a lesson based on the given parameters and uploads it to the provided profile, curriculum, and module.
     *
     * @param title The title of the lesson to be generated.
     * @param profile The profile to which the lesson belongs.
     * @param curriculum The curriculum under which the lesson is categorized.
     * @param module The module under which the lesson is organized.
     * @return A [Result] wrapping the generated and successfully uploaded [Lesson], or an exception if the process fails.
     */
    suspend operator fun invoke(
        title: String,
        profile: Profile,
        curriculum: Curriculum,
        module: Module
    ): Result<Lesson> = try {
        val lesson = generateLesson(title, profile, curriculum, module).first().getOrThrow()
            .run { Lesson(title = title, description = description, content = content) }

        uploadLessonUseCase(lesson, profile.id, curriculum.id, module.id).getOrThrow()

        Result.success(lesson)
    } catch (e: Exception) {
        Result.failure(e)
    }
}