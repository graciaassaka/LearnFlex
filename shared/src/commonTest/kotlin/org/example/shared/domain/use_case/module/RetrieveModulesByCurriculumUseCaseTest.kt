package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrieveModulesByCurriculumUseCaseTest {
    private lateinit var repository: ModuleRepository
    private lateinit var useCase: RetrieveModulesByCurriculumUseCase

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = RetrieveModulesByCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return modules when repository succeeds`() = runTest {
        // Arrange
        val curriculumId = "curriculum456"
        val module1 = mockk<Module>()
        val module2 = mockk<Module>()
        val modulesList = listOf(module1, module2)
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(modulesList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(modulesList), result)
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        // Arrange
        val curriculumId = "curriculumEmptyModules"
        val emptyList: List<Module> = emptyList()
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(emptyList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(emptyList), result)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val curriculumId = "curriculumErrorModules"
        val exception = RuntimeException("Failed to fetch modules")
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.failure(exception)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when repository throws`() {
        // Arrange
        val curriculumId = "curriculumExceptionModules"
        val exception = IllegalArgumentException("Invalid curriculum ID")
        coEvery { repository.getByCurriculumId(curriculumId) } throws exception

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(curriculumId)
            }
        }
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
    }
}
