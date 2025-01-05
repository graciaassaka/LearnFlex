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

class UploadLessonsUseCaseTest {
    private lateinit var uploadLessonsUseCase: UploadLessonsUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        uploadLessonsUseCase = UploadLessonsUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val path = "test/path"
        val lesson = listOf(mockk<Lesson>(relaxed = true))
        coEvery { repository.insertAll(any(), any(), any()) } returns Result.success(Unit)

        // Act
        val result = uploadLessonsUseCase(path, lesson)

        // Assert
        coVerify(exactly = 1) { repository.insertAll(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val path = "test/path"
        val lesson = listOf(mockk<Lesson>(relaxed = true))
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insertAll(any(), any(), any()) } returns Result.failure(exception)

        // Act
        val result = uploadLessonsUseCase(path, lesson)

        // Assert
        coVerify(exactly = 1) { repository.insertAll(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}