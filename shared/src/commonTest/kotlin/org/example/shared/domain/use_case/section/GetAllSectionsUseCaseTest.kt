package org.example.shared.domain.use_case.section

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.data.remote.firestore.FirestorePathBuilder
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetAllSectionsUseCaseTest {
    private lateinit var useCase: GetAllSectionsUseCase
    private lateinit var repository: SectionRepository

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = GetAllSectionsUseCase(repository)
    }

    @Test
    fun `invoke should return sections flow when getAll succeeds`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildSectionPath("userId", "curriculumId", "moduleId", "lessonId")
        val sections = listOf(mockk<Section>())
        val sectionsFlow = flowOf(Result.success(sections))
        every { repository.getAll(path) } returns sectionsFlow

        // Act
        val emissions = mutableListOf<Result<List<Section>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assertEquals(Result.success(sections), emissions.first())
    }

    @Test
    fun `invoke should return error flow when getAll fails`() = runTest {
        // Arrange
        val path = FirestorePathBuilder().buildSectionPath("userId", "curriculumId", "moduleId", "lessonId")
        val exception = RuntimeException("GetAll failed")
        val errorFlow = flowOf(Result.failure<List<Section>>(exception))
        every { repository.getAll(path) } returns errorFlow

        // Act
        val emissions = mutableListOf<Result<List<Section>>>()
        useCase(path).collect { emissions.add(it) }

        // Assert
        verify(exactly = 1) { repository.getAll(path) }
        assertEquals(1, emissions.size)
        assert(emissions.first().isFailure)
        assertEquals(exception, emissions.first().exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when path does not end with SECTIONS`() {
        // Arrange
        val path = FirestorePathBuilder().buildSectionPath("userId", "curriculumId", "moduleId", "lessonId") + "/extra"

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(path).first().getOrThrow()
            }
        }
    }
}