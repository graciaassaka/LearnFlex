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

class UploadCurriculumUseCaseTest {
    private lateinit var uploadCurriculumUseCase: UploadCurriculumUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        uploadCurriculumUseCase = UploadCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculum = mockk<Curriculum> {
            every { id } returns "curriculumId"
        }
        coEvery { repository.insert(any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = uploadCurriculumUseCase(curriculum, userId)

        // Assert
        coVerify(exactly = 1) { repository.insert(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculum = mockk<Curriculum> {
            every { id } returns "curriculumId"
        }
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(any(), any(), any()) } returns Result.failure(exception)

        // Act
        val result = uploadCurriculumUseCase(curriculum, userId)

        // Assert
        coVerify(exactly = 1) { repository.insert(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}