package org.example.shared.domain.use_case.library

import kotlinx.coroutines.*
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.model.Module
import org.example.shared.domain.use_case.curriculum.DeleteCurriculumUseCase
import org.example.shared.domain.use_case.curriculum.UploadCurriculumUseCase
import org.example.shared.domain.use_case.module.DeleteModulesByCurriculumUseCase
import org.example.shared.domain.use_case.module.UploadModulesUseCase
import org.example.shared.domain.use_case.util.CompoundException

/**
 * Use case for uploading a curriculum and its associated modules.
 * This operation ensures that both the curriculum and modules are uploaded
 * in a coordinated manner, and provides mechanisms for rollback in case of failure.
 *
 * @property uploadCurriculum Use case responsible for uploading the curriculum.
 * @property uploadModules Use case responsible for uploading the modules.
 * @property deleteCurriculum Use case responsible for deleting the uploaded curriculum during rollback.
 * @property deleteModules Use case responsible for deleting uploaded modules during rollback.
 */
class UploadCurriculumAndModulesUseCase(
    private val uploadCurriculum: UploadCurriculumUseCase,
    private val uploadModules: UploadModulesUseCase,
    private val deleteCurriculum: DeleteCurriculumUseCase,
    private val deleteModules: DeleteModulesByCurriculumUseCase
) {
    /**
     * Orchestrates the upload process for a curriculum and its associated modules.
     * Handles concurrency for upload operations and ensures a rollback is performed in case of failure.
     *
     * @param userId The ID of the user who owns the curriculum and modules.
     * @param curriculum The curriculum to be uploaded.
     * @param modules The list of modules to be uploaded, associated with the curriculum.
     */
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

    /**
     * Rolls back a curriculum upload operation by attempting to delete the uploaded curriculum
     * if the upload operation was successful.
     *
     * @param curriculum The curriculum to be deleted during the rollback process.
     * @param userId The ID of the user associated with the curriculum upload.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Deferred<Result<Unit>>.rollbackCurriculumUpload(curriculum: Curriculum, userId: String) =
        getCompleted().let { if (it.isSuccess) deleteCurriculum(curriculum, userId).exceptionOrNull() else null }

    /**
     * Attempts to rollback the upload of modules by deleting the specified modules
     * if the associated deferred upload operation was successful.
     *
     * @param modules The list of modules to attempt to delete during rollback.
     * @param userId The ID of the user associated with the modules.
     * @param curriculumId The ID of the curriculum associated with the modules.
     * @return An exception if the rollback process encounters an error, or null if successful.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun Deferred<Result<Unit>>.rollbackModuleUpload(modules: List<Module>, userId: String, curriculumId: String) =
        getCompleted().let { if (it.isSuccess) deleteModules(modules, userId, curriculumId).exceptionOrNull() else null }
}