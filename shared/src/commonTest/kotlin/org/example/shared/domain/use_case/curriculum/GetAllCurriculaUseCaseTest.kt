package org.example.shared.domain.use_case.curriculum

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Assert.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAllCurriculaUseCaseTest {
    private lateinit var useCase: GetAllCurriculaUseCase
    private lateinit var repository: CurriculumRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetAllCurriculaUseCase(repository)
    }

    @Test
    fun `invoke should return curricula flow when getAll succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId")
        val curricula = listOf(mockk<Curriculum>())
        val curriculaFlow = flowOf(Result.success(curricula))
        every { repository.getAll(path) } returns curriculaFlow

        // Act
        val result = useCase(path)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId")
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Curriculum>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = useCase(path)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke should throw exception when path does not end with CURRICULA`() {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path).getOrThrow()
            }
        }
    }
}