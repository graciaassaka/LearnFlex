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
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAllModulesUseCaseTest {
    private lateinit var useCase: GetAllModulesUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllModulesUseCase(repository)
    }

    @Test
    fun `invoke should return modules flow when getAll succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildModulePath("userId", "curriculumId")
        val modules = listOf(mockk<Module>())
        val modulesFlow = flowOf(Result.success(modules))
        every { repository.getAll(path) } returns modulesFlow

        // Act
        val emissions = mutableListOf<Result<List<Module>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(modules), emissions.first())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildModulePath("userId", "curriculumId")
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Module>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<List<Module>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
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
                useCase(path).first().getOrThrow()
            }
        }
    }
}