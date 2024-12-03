package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.example.shared.domain.model.Lesson
import org.example.shared.domain.repository.LessonRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLessonUseCaseTest {
    private lateinit var getLessonUseCase: GetLessonUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        getLessonUseCase = GetLessonUseCase(repository)
    }

    @Test
    fun `invoke should return lesson flow when get succeeds`() {
        val path = "test/path"
        val lessonId = "test-id"
        val lesson = mockk<Lesson>()
        val lessonFlow = flowOf(Result.success(lesson))
        every { repository.get(path, lessonId) } returns lessonFlow

        val result = getLessonUseCase(path, lessonId)

        verify(exactly = 1) { repository.get(path, lessonId) }
        assertEquals(lessonFlow, result)
    }

    @Test
    fun `invoke should return error flow when get fails`() {
        val path = "test/path"
        val lessonId = "test-id"
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<Lesson>(exception))
        every { repository.get(path, lessonId) } returns errorFlow

        val result = getLessonUseCase(path, lessonId)

        verify(exactly = 1) { repository.get(path, lessonId) }
        assertEquals(errorFlow, result)
    }
}