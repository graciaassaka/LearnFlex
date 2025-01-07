package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class UpdateModuleUseCaseTest {
    private lateinit var useCase: UpdateModuleUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UpdateModuleUseCase(repository)
    }

    @Test
    fun `update should return success when succeeds`() = runTest {
        val module = mockk<Module> {
            every { id } returns MODULE_ID
        }
        coEvery { repository.update(module, path, any()) } returns Result.success(Unit)

        val result = useCase(module, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.update(module, path, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `update should return failure when fails`() = runTest {
        val module = mockk<Module> {
            every { id } returns MODULE_ID
        }
        val exception = RuntimeException("Update failed")
        coEvery { repository.update(module, path, any()) } returns Result.failure(exception)

        val result = useCase(module, USER_ID, CURRICULUM_ID)

        coVerify(exactly = 1) { repository.update(module, path, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        private const val MODULE_ID = "moduleId"
        private val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .document(CURRICULUM_ID)
            .collection(Collection.MODULES)
            .document(MODULE_ID)
            .build()
    }
}