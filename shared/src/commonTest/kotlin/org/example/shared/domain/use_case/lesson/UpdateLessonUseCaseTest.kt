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

class UpdateLessonUseCaseTest {
    private lateinit var updateLessonUseCase: UpdateLessonUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        updateLessonUseCase = UpdateLessonUseCase(repository)
    }

    @Test
    fun `invoke should return success when update succeeds`() = runTest {
        val path = "test/path"
        val lesson = mockk<Lesson>(relaxed = true)
        coEvery { repository.update(any(), any(), any()) } returns Result.success(Unit)

        val result = updateLessonUseCase(path, lesson)

        coVerify(exactly = 1) { repository.update(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `invoke should return failure when update fails`() = runTest {
        val path = "test/path"
        val lesson = mockk<Lesson>(relaxed = true)
        val exception = RuntimeException("Update failed")
        coEvery { repository.update(any(), any(), any()) } returns Result.failure(exception)

        val result = updateLessonUseCase(path, lesson)

        coVerify(exactly = 1) { repository.update(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}