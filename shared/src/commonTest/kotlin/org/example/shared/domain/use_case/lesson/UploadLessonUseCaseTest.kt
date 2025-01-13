package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UploadLessonUseCaseTest {
    private lateinit var uploadLessonUseCase: UploadLessonUseCase
    private lateinit var repository: LessonRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        uploadLessonUseCase = UploadLessonUseCase(repository)
    }

    @Test
    fun `invoke should return success when upload succeeds`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculumId = "curriculumId"
        val moduleId = "moduleId"
        val lesson = mockk<Lesson> {
            every { id } returns "lessonId"
        }

        // Construct expected path (if needed for verification)
        PathBuilder().collection(Collection.PROFILES)
            .document(userId)
            .collection(Collection.CURRICULA)
            .document(curriculumId)
            .collection(Collection.MODULES)
            .document(moduleId)
            .collection(Collection.LESSONS)
            .document(lesson.id)
            .build()

        coEvery { repository.insert(item = lesson, path = any(), timestamp = any()) } returns Result.success(Unit)

        // Act
        val result = uploadLessonUseCase(lesson, userId, curriculumId, moduleId)

        // Assert
        coVerify(exactly = 1) { repository.insert(item = lesson, path = any(), timestamp = any()) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when upload fails`() = runTest {
        // Arrange
        val userId = "userId"
        val curriculumId = "curriculumId"
        val moduleId = "moduleId"
        val lesson = mockk<Lesson> {
            every { id } returns "lessonId"
        }
        val exception = RuntimeException("Upload failed")

        coEvery { repository.insert(item = lesson, path = any(), timestamp = any()) } returns Result.failure(exception)

        // Act
        val result = uploadLessonUseCase(lesson, userId, curriculumId, moduleId)

        // Assert
        coVerify(exactly = 1) { repository.insert(item = lesson, path = any(), timestamp = any()) }
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
