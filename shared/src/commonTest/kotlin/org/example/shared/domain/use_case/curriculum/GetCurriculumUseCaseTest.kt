package org.example.shared.domain.use_case.curriculum

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Curriculum
import org.example.shared.domain.repository.CurriculumRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetCurriculumUseCaseTest {
    private lateinit var useCase: GetCurriculumUseCase
    private lateinit var repository: CurriculumRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return curriculum flow when get succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId")
        val curriculum = mockk<Curriculum>()
        val curriculumFlow = flowOf(Result.success(curriculum))
        every { repository.get(path, "curriculumId") } returns curriculumFlow

        // Act
        val emissions = mutableListOf<Result<Curriculum>>()
        useCase(path, "curriculumId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "curriculumId") }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(curriculum), emissions.first())
    }

    @Test
    fun `invoke should return error flow when get fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId")
        val exception = RuntimeException("Get failed")
        every { repository.get(path, "curriculumId") } throws exception

        // Act
        val emissions = mutableListOf<Result<Curriculum>>()
        useCase(path, "curriculumId").collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.get(path, "curriculumId") }
        assertEquals(1, emissions.size)
        assert(emissions.first().isFailure)
        assertEquals(exception, emissions.first().exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when path does not end with CURRICULA`() {
        // Arrange
        val path = FirestorePathBuilder().buildCurriculumPath("userId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path, "curriculumId").first().getOrThrow()
            }
        }
    }
}