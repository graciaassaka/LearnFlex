package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class DeleteAllModulesUseCaseTest {
    private lateinit var useCase: DeleteModulesByCurriculumUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = DeleteModulesByCurriculumUseCase(repository)
    }

    @Test
    fun `deleteAll should return success when succeeds`() = runTest {
        val modules = listOf(mockk<Module>())
        coEvery { repository.deleteAll(any(), any(), any()) } returns Result.success(Unit)

        val result = useCase(modules, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.deleteAll(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `deleteAll should return failure when fails`() = runTest {
        val modules = listOf(mockk<Module>())
        val exception = RuntimeException("Delete failed")
        coEvery { repository.deleteAll(any(), any(), any()) } returns Result.failure(exception)

        val result = useCase(modules, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.deleteAll(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
    }
}