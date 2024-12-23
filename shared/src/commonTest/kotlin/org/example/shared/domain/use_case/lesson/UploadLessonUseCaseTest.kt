package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class UploadLessonUseCaseTest {
    private lateinit var uploadLessonUseCase: UploadLessonUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        uploadLessonUseCase = UploadLessonUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val lesson = mockk<Lesson>(relaxed = true)
        coEvery { repository.insert(any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = uploadLessonUseCase(path, lesson)

        // Assert
        coVerify(exactly = 1) { repository.insert(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val path = "test/path"
        val lesson = mockk<Lesson>(relaxed = true)
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(any(), any(), any()) } returns Result.failure(exception)

        // Act
        val result = uploadLessonUseCase(path, lesson)

        // Assert
        coVerify(exactly = 1) { repository.insert(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}