package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteAllCurriculaUseCaseTest {
    private lateinit var deleteAllCurriculaUseCase: DeleteAllCurriculaUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        deleteAllCurriculaUseCase = DeleteAllCurriculaUseCase(repository)
    }

    @Test
    fun `invoke should return success when deletion succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val curricula = List(3) { mockk<Curriculum>(relaxed = true) }
        coEvery { repository.deleteAll(path, curricula) } returns Result.success(Unit)

        // Act
        val result = deleteAllCurriculaUseCase(path, curricula)

        // Assert
        coVerify(exactly = 1) { repository.deleteAll(path, curricula) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when deletion fails`() = runTest {
        // Arrange
        val path = "test/path"
        val curricula = List(3) { mockk<Curriculum>(relaxed = true) }
        val exception = RuntimeException("Delete failed")
        coEvery { repository.deleteAll(path, curricula) } returns Result.failure(exception)

        // Act
        val result = deleteAllCurriculaUseCase(path, curricula)

        // Assert
        coVerify(exactly = 1) { repository.deleteAll(path, curricula) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should return success when deleting empty list`() = runTest {
        // Arrange
        val path = "test/path"
        val curricula = emptyList<Curriculum>()
        coEvery { repository.deleteAll(path, curricula) } returns Result.success(Unit)

        // Act
        val result = deleteAllCurriculaUseCase(path, curricula)

        // Assert
        coVerify(exactly = 1) { repository.deleteAll(path, curricula) }
        assert(result.isSuccess)
    }
}