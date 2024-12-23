package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.constant.ContentStatus
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
                if (it.quizScore >= it.quizScoreMax * 0.75) ContentStatus.FINISHED
                else ContentStatus.UNFINISHED
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
                title = "Section 1",
                description = "Description 1",
                imageUrl = "https://example.com/image1.png",
                index = 1,
                quizScore = 5,
                quizScoreMax = 10,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                content = "Content 1"
            ),
            Section(
                id = "2",
                title = "Section 2",
                description = "Description 2",
                imageUrl = "https://example.com/image2.png",
                index = 2,
                quizScore = 6,
                quizScoreMax = 10,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                content = "Content 2"
            ),
            Section(
                id = "3",
                title = "Section 3",
                description = "Description 3",
                imageUrl = "https://example.com/image3.png",
                index = 3,
                quizScore = 7,
                quizScoreMax = 10,
                createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                lastUpdated = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                content = "Content 3"
            )
        )
    }
}