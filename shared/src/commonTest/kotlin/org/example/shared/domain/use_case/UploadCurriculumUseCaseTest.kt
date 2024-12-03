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
        val path = "test/path"
        val curriculum = mockk<Curriculum>(relaxed = true)
        coEvery { repository.insert(path, curriculum) } returns Result.success(Unit)

        // Act
        val result = uploadCurriculumUseCase(path, curriculum)

        // Assert
        coVerify(exactly = 1) { repository.insert(path, curriculum) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val path = "test/path"
        val curriculum = mockk<Curriculum>(relaxed = true)
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(path, curriculum) } returns Result.failure(exception)

        // Act
        val result = uploadCurriculumUseCase(path, curriculum)

        // Assert
        coVerify(exactly = 1) { repository.insert(path, curriculum) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}