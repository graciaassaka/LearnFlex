package org.example.shared.domain.use_case.module

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchModulesByCurriculumUseCaseTest {
    private lateinit var useCase: FetchModulesByCurriculumUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = FetchModulesByCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return modules flow when getAll succeeds`() = runTest {
        // Arrange
        val modules = listOf(mockk<Module>())
        val modulesFlow = flowOf(Result.success(modules))
        every { repository.getAll(path) } returns modulesFlow

        // Act
        val result = useCase(USER_ID, CURRICULUM_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(Result.success(modules), result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Module>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = useCase(USER_ID, CURRICULUM_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }


    companion object {
        private const val USER_ID = "userId"
        private const val CURRICULUM_ID = "curriculumId"
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .document(CURRICULUM_ID)
            .collection(Collection.MODULES)
            .build()
    }
}