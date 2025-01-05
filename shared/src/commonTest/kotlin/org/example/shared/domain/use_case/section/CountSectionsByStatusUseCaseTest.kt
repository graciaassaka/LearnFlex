package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.Status
import org.example.shared.domain.model.Section
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals

class CountSectionsByStatusUseCaseTest {
    private lateinit var countSectionsByStatusUseCase: CountSectionsByStatusUseCase
    private lateinit var getSectionsByCurriculumIdUseCase: GetSectionsByCurriculumIdUseCase

    @Before
    fun setUp() {
        getSectionsByCurriculumIdUseCase = mockk<GetSectionsByCurriculumIdUseCase>()
        countSectionsByStatusUseCase = CountSectionsByStatusUseCase(getSectionsByCurriculumIdUseCase)
    }

    @Test
    fun `invoke should return Result#success encapsulating a map of ContentStatus to section number when getAllSections returns Result#success`() =
        runTest {
            // Arrange
            val sectionProgress = sections.groupBy {
                if (it.quizScore >= it.quizScoreMax * 0.75) Status.FINISHED
                else Status.UNFINISHED
            }.mapValues { it.value.size }

            coEvery { getSectionsByCurriculumIdUseCase(PATH) } returns Result.success(sections)

            // Act
            val result = countSectionsByStatusUseCase(PATH)

            // Assert
            assert(result.isSuccess)
            assertEquals(sectionProgress, result.getOrNull())
        }

    @Test
    fun `invoke should return Result#failure when getAllSections returns Result#failure`() = runTest {
        // Arrange
        val exception = Exception("An error occurred")
        coEvery { getSectionsByCurriculumIdUseCase(PATH) } returns Result.failure(exception)

        // Act
        val result = countSectionsByStatusUseCase(PATH)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull() == exception)
    }

    companion object {
        private const val PATH = "test/path"
        private val sections = listOf(
            Section(
                id = "1",
                index = 1,
                title = "Section 1",
                description = "Description 1",
                content = "Content 1",
                quizScore = 5,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
            Section(
                id = "2",
                index = 2,
                title = "Section 2",
                description = "Description 2",
                content = "Content 2",
                quizScore = 6,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ),
            Section(
                id = "3",
                index = 3,
                title = "Section 3",
                description = "Description 3",
                content = "Content 3",
                quizScore = 7,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        )
    }
}