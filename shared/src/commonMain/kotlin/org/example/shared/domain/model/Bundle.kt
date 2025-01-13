package org.example.shared.domain.model

/**
 * Bundle is a container that organizes and maps curriculum components such as modules, lessons, and sections.
 *
 * @property curriculum The curriculum associated with this bundle.
 * @property modules A map of ModuleKey objects to their corresponding Module instances.
 * @property lessons A map of LessonKey objects to their corresponding Lesson instances.
 * @property sections A map of SectionKey objects to their corresponding Section instances.
 */
data class Bundle(
    val curriculum: Curriculum,
    val modules: Map<ModuleKey, Module>,
    val lessons: Map<LessonKey, Lesson>,
    val sections: Map<SectionKey, Section>
) {
    /**
     * Represents a unique key for identifying a module within a curriculum.
     *
     * @property curriculumId The unique identifier of the curriculum.
     * @property moduleId The unique identifier of the module within the specified curriculum.
     */
    data class ModuleKey(val curriculumId: String, val moduleId: String)

    /**
     * Represents a unique key identifying a specific lesson within a curriculum.
     *
     * @property curriculumId The identifier of the curriculum to which the lesson belongs.
     * @property moduleId The identifier of the module to which the lesson belongs.
     * @property lessonId The identifier of the specific lesson.
     */
    data class LessonKey(val curriculumId: String, val moduleId: String, val lessonId: String)

    /**
     * Represents a unique key identifying a specific section within a lesson.
     *
     * @property curriculumId The identifier of the curriculum to which the section belongs.
     * @property moduleId The identifier of the module to which the section belongs.
     * @property lessonId The identifier of the lesson to which the section belongs.
     * @property sectionId The identifier of the specific section.
     */
    data class SectionKey(val curriculumId: String, val moduleId: String, val lessonId: String, val sectionId: String)

    /**
     * Retrieves all modules contained in this bundle.
     *
     * @return A list of all modules in this bundle.
     */
    fun getModules() = modules.values.toList()

    /**
     * Retrieves all lessons contained in this bundle.
     *
     * @return A list of all lessons in this bundle.
     */
    fun getLessons() = lessons.values.toList()

    /**
     * Retrieves all sections contained in this bundle.
     *
     * @return A list of all sections in this bundle.
     */
    fun getSections() = sections.values.toList()

    /**
     * Retrieves all lessons contained in this bundle.
     *
     * @return A list of all lessons in this bundle.
     * @param moduleKey The key of the module whose lessons are to be retrieved.
     */
    fun getLessonsByModule(moduleKey: ModuleKey) = lessons.filterKeys {
        it.curriculumId == moduleKey.curriculumId &&
                it.moduleId == moduleKey.moduleId
    }.values.toList()

    /**
     * Retrieves all sections contained in this bundle.
     *
     * @return A list of all sections in this bundle.
     * @param lessonKey The key of the lesson whose sections are to be retrieved.
     */
    fun getSectionsByLesson(lessonKey: LessonKey) = sections.filterKeys {
        it.curriculumId == lessonKey.curriculumId &&
                it.moduleId == lessonKey.moduleId &&
                it.lessonId == lessonKey.lessonId
    }.values.toList()
}