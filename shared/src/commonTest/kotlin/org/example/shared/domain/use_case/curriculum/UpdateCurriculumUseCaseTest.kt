package org.example.shared.domain.use_case.curriculum

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class UpdateCurriculumUseCaseTest {
    private lateinit var updateCurriculumUseCase: UpdateCurriculumUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        updateCurriculumUseCase = UpdateCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return success when update succeeds`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculum = mockk<Curriculum> {
            every { id } returns "curriculumId"
        }
        coEvery { repository.update(any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = updateCurriculumUseCase(curriculum, userId)

        // Assert
        coVerify(exactly = 1) { repository.update(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when update fails`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculum = mockk<Curriculum> {
            every { id } returns "curriculumId"
        }
        val exception = RuntimeException("Update failed")
        coEvery { repository.update(any(), any(), any()) } returns Result.failure(exception)

        // Act
        val result = updateCurriculumUseCase(curriculum, userId)

        // Assert
        coVerify(exactly = 1) { repository.update(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}