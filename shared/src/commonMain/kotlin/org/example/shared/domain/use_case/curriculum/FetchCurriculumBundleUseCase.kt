package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.example.shared.domain.model.*
import org.example.shared.domain.use_case.lesson.FetchLessonsByModuleUseCase
import org.example.shared.domain.use_case.module.FetchModulesByCurriculumUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase

/**
 * Use case to fetch curriculum bundles by aggregating modules, lessons for each module,
 * and sections for each lesson belonging to the curriculum.
 * This class orchestrates multiple use cases to structure the curriculum data hierarchically.
 *
 * @property fetchModules The use case to fetch modules by curriculum.
 * @property fetchLessons The use case to fetch lessons by module.
 * @property fetchSections The use case to fetch sections by lesson.
 */
class FetchCurriculumBundleUseCase(
    private val fetchModules: FetchModulesByCurriculumUseCase,
    private val fetchLessons: FetchLessonsByModuleUseCase,
    private val fetchSections: FetchSectionsByLessonUseCase,
    private val updateCurriculumUseCase: UpdateCurriculumUseCase
) {
    /**
     * Represents a module along with its associated lessons and their sections.
     *
     * @property module The module containing details such as title, description, and quiz score.
     * @property lessons A list of lessons associated with the module, each lesson includes its sections.
     */
    private data class ModuleWithLessons(
        val module: Module,
        val lessons: List<LessonWithSections>
    )

    /**
     * Represents a composite entity combining a lesson with its associated sections.
     *
     * @property lesson The lesson associated with this entity.
     * @property sections The list of sections that belong to the lesson.
     */
    private data class LessonWithSections(
        val lesson: Lesson,
        val sections: List<Section>
    )

    /**
     * Fetches and processes all modules, lessons, and sections for a given curriculum and user,
     * returning a structured Bundle containing the entire curriculum data.
     *
     * @param userId The ID of the user for whom the curriculum is being fetched.
     * @param curriculum The curriculum whose data is to be retrieved and processed into a structured Bundle.
     * @return A Bundle containing the processed curriculum data, including modules, lessons, and sections.
     */
    suspend operator fun invoke(userId: String, curriculum: Curriculum): Bundle = supervisorScope {
        val modules = fetchModules(userId, curriculum.id).getOrNull().orEmpty()
        val updatedCurriculum = async { updateCurriculum(userId, curriculum, modules) }
        val moduleWithLessons = fetchModules(userId, curriculum.id).getOrNull().orEmpty().map { module ->
            async {
                val lessonWithSections = fetchLessons(userId, curriculum.id, module.id).getOrNull().orEmpty().map { lesson ->
                    async {
                        val sections = fetchSections(userId, curriculum.id, module.id, lesson.id).getOrNull().orEmpty()
                        LessonWithSections(lesson, sections)
                    }
                }.map { it.await() }
                ModuleWithLessons(module, lessonWithSections)
            }
        }.map { it.await() }

        moduleWithLessons.process(updatedCurriculum.await())
    }

    /**
     * Updates the specified curriculum's status based on the provided modules and, if changes are detected,
     * synchronizes the updated curriculum to the repository via the use case.
     *
     * @param userId The ID of the user associated with the curriculum.
     * @param curriculum The curriculum to update.
     * @param modules The list of modules to evaluate for updating the curriculum's status.
     * @return A new instance of the curriculum with the updated status.
     */
    private suspend fun updateCurriculum(
        userId: String,
        curriculum: Curriculum,
        modules: List<Module>
    ): Curriculum {
        val updatedCurriculum = curriculum.updateStatus(modules)
        if (updatedCurriculum != curriculum) updateCurriculumUseCase(updatedCurriculum, userId)
        return updatedCurriculum
    }

    /**
     * Processes a list of ModuleWithLessons and maps their respective curriculum, modules, lessons, and sections
     * to organized data structures. The function generates a Bundle containing these mappings.
     *
     * @param curriculum The curriculum associated with the modules and their contents.
     * @return A Bundle that contains mappings of curriculum modules, lessons, and sections.
     */
    private fun List<ModuleWithLessons>.process(curriculum: Curriculum): Bundle {
        val modulesMap = mutableMapOf<Bundle.ModuleKey, Module>()
        val lessonsMap = mutableMapOf<Bundle.LessonKey, Lesson>()
        val sectionsMap = mutableMapOf<Bundle.SectionKey, Section>()

        forEach { moduleWithLessons ->
            modulesMap[Bundle.ModuleKey(curriculum.id, moduleWithLessons.module.id)] = moduleWithLessons.module

            moduleWithLessons.lessons.forEach { lessonWithSections ->
                lessonsMap[Bundle.LessonKey(curriculum.id, moduleWithLessons.module.id, lessonWithSections.lesson.id)] =
                    lessonWithSections.lesson

                lessonWithSections.sections.forEach { section ->
                    sectionsMap[Bundle.SectionKey(curriculum.id, moduleWithLessons.module.id, lessonWithSections.lesson.id, section.id)] =
                        section
                }
            }
        }

        return Bundle(curriculum, modulesMap, lessonsMap, sectionsMap)
    }
}
