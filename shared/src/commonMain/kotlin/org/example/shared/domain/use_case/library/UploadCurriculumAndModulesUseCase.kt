package org.example.shared.domain.use_case.library

import kotlinx.coroutines.*
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.use_case.curriculum.DeleteCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.UploadCurriculumUseCase
import org.example.shared.domain.use_case.module.DeleteModulesByCurriculumUseCase
import org.example.shared.domain.use_case.module.UploadModulesUseCase
import org.example.shared.domain.use_case.util.CompoundException

class UploadCurriculumAndModulesUseCase(
    private val uploadCurriculum: UploadCurriculumUseCase,
    private val uploadModules: UploadModulesUseCase,
    private val deleteCurriculum: DeleteCurriculumUseCase,
    private val deleteModules: DeleteModulesByCurriculumUseCase
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        userId: String,
        curriculum: Curriculum,
        modules: List<Module>
    ) = coroutineScope {
        val uploadJobs = listOf(
            async { uploadCurriculum(curriculum, userId) },
            async { uploadModules(modules, userId, curriculum.id) }
        )
        try {
            uploadJobs.awaitAll().forEach { it.getOrThrow() }
            Result.success(Unit)
        } catch (e: Exception) {
            listOf(
                async { uploadJobs.first().rollbackCurriculumUpload(curriculum, userId) },
                async { uploadJobs.last().rollbackModuleUpload(modules, userId, curriculum.id) }
            ).awaitAll().run {
                if (all { it == null }) Result.failure(e)
                else Result.failure(CompoundException(e, *filterNotNull().toTypedArray()))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Deferred<Result<Unit>>.rollbackCurriculumUpload(curriculum: Curriculum, userId: String) =
        getCompleted().let { if (it.isSuccess) deleteCurriculum(curriculum, userId).exceptionOrNull() else null }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Deferred<Result<Unit>>.rollbackModuleUpload(modules: List<Module>, userId: String, curriculumId: String) =
        getCompleted().let { if (it.isSuccess) deleteModules(modules, userId, curriculumId).exceptionOrNull() else null }
}