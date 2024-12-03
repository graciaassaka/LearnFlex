package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCurriculumUseCaseTest {
    private lateinit var getCurriculumUseCase: GetCurriculumUseCase
    private lateinit var repository: CurriculumRepository

    @Before
    fun setUp() {
        repository = mockk()
        getCurriculumUseCase = GetCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return success with curriculum`() = runTest {
        // Arrange
        val path = "test/path"
        val id = "test-id"
        val curriculum = mockk<Curriculum>(relaxed = true)
        val flow = flowOf(Result.success(curriculum))
        every { repository.get(path, id) } returns flow

        // Act
        val result = getCurriculumUseCase(path, id).first()

        // Assert
        verify(exactly = 1) { repository.get(path, id) }
        assert(result.isSuccess)
        assertEquals(curriculum, result.getOrNull())
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val path = "test/path"
        val id = "test-id"
        val exception = RuntimeException("Failed to get curriculum")
        val flow = flowOf(Result.failure<Curriculum>(exception))
        every { repository.get(path, id) } returns flow

        // Act
        val result = getCurriculumUseCase(path, id).first()

        // Assert
        verify(exactly = 1) { repository.get(path, id) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}