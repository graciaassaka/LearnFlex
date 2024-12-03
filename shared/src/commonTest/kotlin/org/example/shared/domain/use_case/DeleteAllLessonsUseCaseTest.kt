package org.example.shared.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteAllLessonsUseCaseTest {
    private lateinit var deleteAllLessonsUseCase: DeleteAllLessonsUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        deleteAllLessonsUseCase = DeleteAllLessonsUseCase(repository)
    }

    @Test
    fun `invoke should return success when deleteAll succeeds`() = runTest {
        val path = "test/path"
        val lessons = listOf(mockk<Lesson>(relaxed = true), mockk(relaxed = true))
        coEvery { repository.deleteAll(path, lessons) } returns Result.success(Unit)

        val result = deleteAllLessonsUseCase(path, lessons)

        coVerify(exactly = 1) { repository.deleteAll(path, lessons) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when deleteAll fails`() = runTest {
        val path = "test/path"
        val lessons = listOf(mockk<Lesson>(relaxed = true), mockk(relaxed = true))
        val exception = RuntimeException("DeleteAll failed")
        coEvery { repository.deleteAll(path, lessons) } returns Result.failure(exception)

        val result = deleteAllLessonsUseCase(path, lessons)

        coVerify(exactly = 1) { repository.deleteAll(path, lessons) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}