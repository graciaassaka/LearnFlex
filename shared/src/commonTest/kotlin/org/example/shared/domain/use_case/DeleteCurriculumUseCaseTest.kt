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

class DeleteCurriculumUseCaseTest {
    private lateinit var deleteCurriculumUseCase: DeleteCurriculumUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        deleteCurriculumUseCase = DeleteCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return success when deletion succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val curriculum = mockk<Curriculum>(relaxed = true)
        coEvery { repository.delete(path, curriculum) } returns Result.success(Unit)

        // Act
        val result = deleteCurriculumUseCase(path, curriculum)

        // Assert
        coVerify(exactly = 1) { repository.delete(path, curriculum) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when deletion fails`() = runTest {
        // Arrange
        val path = "test/path"
        val curriculum = mockk<Curriculum>(relaxed = true)
        val exception = RuntimeException("Delete failed")
        coEvery { repository.delete(path, curriculum) } returns Result.failure(exception)

        // Act
        val result = deleteCurriculumUseCase(path, curriculum)

        // Assert
        coVerify(exactly = 1) { repository.delete(path, curriculum) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}