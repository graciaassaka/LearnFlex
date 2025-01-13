package org.example.shared.domain.model.util

import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Bundle
import org.example.shared.domain.model.interfaces.ScorableRecord

/**
 * Manages a list of bundles and provides various utility functions to access and count their contents.
 *
 * @property bundles The list of bundles to manage.
 */
data class BundleManager(val bundles: List<Bundle>) {

    /**
     * Retrieves the curricula from all bundles.
     *
     * @return A list of curricula.
     */
    fun getCurricula() = bundles.map { it.curriculum }

    /**
     * Retrieves a curriculum by its key or the most recently updated curriculum if the key is not found.
     *
     * @param key The key of the curriculum to retrieve.
     * @return The curriculum if found, the most recent curriculum otherwise, or null if no curricula are available.
     */
    fun getCurriculumByKeyOrLatest(key: String) =
        getCurriculumByKey(key) ?: getLatestCurriculum()

    /**
     * Retrieves a curriculum by its key.
     *
     * @param key The key of the curriculum to retrieve.
     * @return The curriculum if found, null otherwise.
     */
    fun getCurriculumByKey(key: String) =
        bundles.findByCurriculumId(key)?.curriculum


    /**
     * Retrieves the most recently updated curriculum across all available bundles.
     *
     * @return The most recent curriculum based on the `lastUpdated` timestamp, or null if no curricula are available.
     */
    fun getLatestCurriculum() =
        bundles.map { it.curriculum }.maxByOrNull { it.lastUpdated }

    /**
     * Retrieves the modules of a curriculum by its key.
     *
     * @param key The key of the curriculum.
     * @return A list of modules if found, null otherwise.
     */
    fun getModulesByCurriculum(key: String) =
        bundles.findByCurriculumId(key)?.getModules().orEmpty()

    /**
     * Retrieves a module by its key or the most recently updated module in the curriculum if the key is not found.
     *
     * @param key The key of the module to retrieve.
     * @return The module if found, the most recent module otherwise, or null if no modules are available.
     */
    fun getModuleByKeyOrLatest(key: Bundle.ModuleKey) =
        getModuleByKey(key) ?: getLatestModule(key.curriculumId)

    /**
     * Retrieves a module by its key.
     *
     * @param key The key of the module to retrieve.
     * @return The module if found, null otherwise.
     */
    fun getModuleByKey(key: Bundle.ModuleKey) =
        bundles.findByCurriculumId(key.curriculumId)?.modules?.get(key)

    /**
     * Retrieves the most recently updated module in a specific curriculum.
     *
     * @param key The key of the curriculum.
     * @return The most recent module based on the `lastUpdated` timestamp, or null if no modules are available.
     */
    fun getLatestModule(key: String) =
        bundles.findByCurriculumId(key)?.modules?.values?.maxByOrNull { it.lastUpdated }

    /**
     * Retrieves the lessons of a module by its key.
     *
     * @param key The key of the module.
     * @return A list of lessons if found, null otherwise.
     */
    fun getLessonsByModule(key: Bundle.ModuleKey) =
        bundles.findByCurriculumId(key.curriculumId)?.getLessonsByModule(key).orEmpty()

    /**
     * Retrieves a lesson by its key or the most recently updated lesson in the module if the key is not found.
     *
     * @param key The key of the lesson to retrieve.
     * @return The lesson if found, the most recent lesson otherwise, or null if no lessons are available.
     */
    fun getLessonByKeyOrLatest(key: Bundle.LessonKey) =
        getLessonByKey(key) ?: getLatestLessonInModule(Bundle.ModuleKey(key.curriculumId, key.moduleId))

    /**
     * Retrieves a lesson by its key.
     *
     * @param key The key of the lesson to retrieve.
     * @return The lesson if found, null otherwise.
     */
    fun getLessonByKey(key: Bundle.LessonKey) =
        bundles.findByCurriculumId(key.curriculumId)?.lessons?.get(key)

    /**
     * Retrieves the most recently updated lesson in a specific module.
     *
     * @param key The key of the module.
     * @return The most recent lesson based on the `lastUpdated` timestamp, or null if no lessons are available.
     */
    fun getLatestLessonInModule(key: Bundle.ModuleKey) =
        bundles.findByCurriculumId(key.curriculumId)?.getLessonsByModule(key)?.maxByOrNull { it.lastUpdated }

    /**
     * Retrieves the sections of a lesson by its key.
     *
     * @param key The key of the lesson.
     * @return A list of sections if found, null otherwise.
     */
    fun getSectionsByLesson(key: Bundle.LessonKey) =
        bundles.findByCurriculumId(key.curriculumId)?.getSectionsByLesson(key).orEmpty()

    /**
     * Counts the modules of a curriculum by their status.
     *
     * @param key The key of the curriculum.
     * @return A map of status to count if found, null otherwise.
     */
    fun countCurriculumModulesByStatus(key: String) =
        bundles.findByCurriculumId(key)?.getModules()?.let { countByStatus(it) }.orEmpty()

    /**
     * Counts the lessons of a curriculum by their status.
     *
     * @param key The key of the curriculum.
     * @return A map of status to count if found, null otherwise.
     */
    fun countCurriculumLessonsByStatus(key: String) =
        bundles.findByCurriculumId(key)?.getLessons()?.let { countByStatus(it) }.orEmpty()

    /**
     * Counts the sections of a curriculum by their status.
     *
     * @param key The key of the curriculum.
     * @return A map of status to count if found, null otherwise.
     */
    fun countCurriculumSectionsByStatus(key: String) =
        bundles.findByCurriculumId(key)?.getSections()?.let { countByStatus(it) }.orEmpty()

    /**
     * Finds a bundle by its curriculum ID.
     *
     * @param curriculumId The ID of the curriculum.
     * @return The bundle if found, null otherwise.
     */
    private fun List<Bundle>.findByCurriculumId(curriculumId: String?) = find { it.curriculum.id == curriculumId }

    /**
     * Counts items by their status.
     *
     * @param T The type of scorable record.
     * @param items The list of items to count.
     * @return A map of status to count.
     */
    private fun <T : ScorableRecord> countByStatus(items: List<T>): Map<Status, Int> =
        items.groupBy { if (it.quizScore >= it.quizScoreMax * 0.75) Status.FINISHED else Status.UNFINISHED }.mapValues { it.value.size }
}