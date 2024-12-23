package org.example.shared.domain.use_case.module

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetModuleUseCaseTest {
    private lateinit var useCase: GetModuleUseCase
    private lateinit var repository: ModuleRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetModuleUseCase(repository)
    }

    @Test
    fun `invoke should return module flow when get succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildModulePath("userId", "curriculumId")
        val module = mockk<Module>()
        val moduleFlow = flowOf(Result.success(module))
        every { repository.get(path, "moduleId") } returns moduleFlow

        // Act
        val emissions = mutableListOf<Result<Module>>()
        useCase(path, "moduleId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "moduleId") }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(module), emissions.first())
    }

    @Test
    fun `invoke should return error flow when get fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildModulePath("userId", "curriculumId")
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<Module>(exception))
        every { repository.get(path, "moduleId") } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<Module>>()
        useCase(path, "moduleId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "moduleId") }
        assertEquals(1, emissions.size)
        assert(emissions.first().isFailure)
        assertEquals(exception, emissions.first().exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when path does not end with MODULES`() {
        // Arrange
        val path = FirestorePathBuilder().buildModulePath("userId", "curriculumId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path, "moduleId").first().getOrThrow()
            }
        }
    }
}