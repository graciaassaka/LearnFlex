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

class GetAllLessonsUseCaseTest {
    private lateinit var getAllLessonsUseCase: GetAllLessonsUseCase
    private lateinit var repository: LessonRepository

    @Before
    fun setUp() {
        repository = mockk()
        getAllLessonsUseCase = GetAllLessonsUseCase(repository)
    }

    @Test
    fun `invoke should return lessons flow when getAll succeeds`() {
        val path = "test/path"
        val lessons = listOf(mockk<Lesson>(), mockk<Lesson>())
        val lessonsFlow = flowOf(Result.success(lessons))
        every { repository.getAll(path) } returns lessonsFlow

        val result = getAllLessonsUseCase(path)

        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(lessonsFlow, result)
    }

    @Test
    fun `invoke should return error flow when getAll fails`() {
        val path = "test/path"
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Lesson>>(exception))
        every { repository.getAll(path) } returns errorFlow

        val result = getAllLessonsUseCase(path)

        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(errorFlow, result)
    }
}