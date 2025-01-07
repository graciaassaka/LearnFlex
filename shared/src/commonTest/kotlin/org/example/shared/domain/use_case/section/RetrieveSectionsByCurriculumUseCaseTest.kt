package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrieveSectionsByCurriculumUseCaseTest {
    private lateinit var repository: SectionRepository
    private lateinit var useCase: RetrieveSectionsByCurriculumUseCase

    @BeforeTest
    fun setUp() {
        repository = mockk()
        useCase = RetrieveSectionsByCurriculumUseCase(repository)
    }

    @Test
    fun `invoke should return sections when repository succeeds with non-empty list`() = runTest {
        // Arrange
        val curriculumId = "curriculum789"
        val section1 = mockk<Section>()
        val section2 = mockk<Section>()
        val sectionsList = listOf(section1, section2)
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(sectionsList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(sectionsList), result)
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        // Arrange
        val curriculumId = "curriculumEmptySections"
        val emptyList: List<Section> = emptyList()
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.success(emptyList)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assertEquals(Result.success(emptyList), result)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Arrange
        val curriculumId = "curriculumErrorSections"
        val exception = RuntimeException("Failed to fetch sections")
        coEvery { repository.getByCurriculumId(curriculumId) } returns Result.failure(exception)

        // Act
        val result = useCase(curriculumId)

        // Assert
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `invoke should throw exception when repository throws`() {
        // Arrange
        val curriculumId = "curriculumExceptionSections"
        val exception = IllegalArgumentException("Invalid curriculum ID")
        coEvery { repository.getByCurriculumId(curriculumId) } throws exception

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            runTest {
                useCase(curriculumId)
            }
        }
        coVerify(exactly = 1) { repository.getByCurriculumId(curriculumId) }
    }
}
