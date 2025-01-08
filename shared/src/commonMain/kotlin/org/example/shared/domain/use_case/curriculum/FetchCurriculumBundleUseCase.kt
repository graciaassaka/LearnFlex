package org.example.shared.domain.use_case.curriculum

import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import org.example.shared.domain.model.Bundle
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.use_case.lesson.FetchLessonsByModuleUseCase
import org.example.shared.domain.use_case.module.FetchModulesByCurriculumUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase

class FetchCurriculumBundleUseCase(
    private val fetchModules: FetchModulesByCurriculumUseCase,
    private val fetchLessons: FetchLessonsByModuleUseCase,
    private val fetchSections: FetchSectionsByLessonUseCase
) {
    suspend operator fun invoke(
        userId: String,
        curriculum: Curriculum
    ) = supervisorScope {
        val modules = fetchModules(userId, curriculum.id).getOrNull().orEmpty()
        modules.map { module ->
            async {
                val lessons = fetchLessons(userId, curriculum.id, module.id).getOrNull().orEmpty()
                val sections = lessons
                    .map { async { fetchSections(userId, curriculum.id, module.id, it.id).getOrNull().orEmpty() } }
                    .map { it.await() }
                lessons to sections.flatten()
            }
        }.map { lessonsSections ->
            lessonsSections.await()
        }.run {
            Bundle(curriculum, modules, flatMap { it.first }, flatMap { it.second })
        }
    }
}
