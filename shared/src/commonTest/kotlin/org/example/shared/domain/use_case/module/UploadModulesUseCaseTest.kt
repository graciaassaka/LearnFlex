package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class UploadModulesUseCaseTest {
    private lateinit var useCase: UploadModulesUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadModulesUseCase(repository)
    }

    @Test
    fun `upload should return success when succeeds`() = runTest {
        val modules = listOf(mockk<Module>())
        coEvery { repository.insertAll(modules, path, any()) } returns Result.success(Unit)

        val result = useCase(modules, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.insertAll(modules, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        val modules = listOf(mockk<Module>())
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insertAll(modules, path, any()) } returns Result.failure(exception)

        val result = useCase(modules, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.insertAll(modules, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .document(CURRICULUM_ID)
            .collection(Collection.MODULES)
            .build()
    }
}