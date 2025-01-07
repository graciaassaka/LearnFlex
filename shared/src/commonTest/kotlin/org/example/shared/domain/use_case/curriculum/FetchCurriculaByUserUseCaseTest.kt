package org.example.shared.domain.use_case.curriculum

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Assert.assertTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchCurriculaByUserUseCaseTest {
    private lateinit var useCase: FetchCurriculaByUserUseCase
    private lateinit var repository: CurriculumRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = FetchCurriculaByUserUseCase(repository)
    }

    @Test
    fun `invoke should return curricula flow when getAll succeeds`() = runTest {
        // Arrange
        val curricula = listOf(mockk<Curriculum>())
        val curriculaFlow = flowOf(Result.success(curricula))
        every { repository.getAll(path) } returns curriculaFlow

        // Act
        val result = useCase(USER_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(curricula, result.getOrNull())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Curriculum>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val result = useCase(USER_ID)

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertTrue(result.isFailure)
        assertEquals(exception.message, result.exceptionOrNull()?.message)
    }


    companion object {
        private const val USER_ID = "userId"
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document(USER_ID)
            .collection(Collection.CURRICULA)
            .build()
    }
}