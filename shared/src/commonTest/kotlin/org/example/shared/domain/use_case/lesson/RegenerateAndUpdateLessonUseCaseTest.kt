package org.example.shared.domain.use_case.lesson

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.model.*
import org.example.shared.domain.use_case.section.DeleteSectionsByLessonUseCase
import org.example.shared.domain.use_case.section.FetchSectionsByLessonUseCase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegenerateAndUpdateLessonUseCaseTest {

    private lateinit var deleteSections: DeleteSectionsByLessonUseCase
    private lateinit var fetchSections: FetchSectionsByLessonUseCase
    private lateinit var generateLesson: GenerateLessonUseCase
    private lateinit var updateLesson: UpdateLessonUseCase
    private lateinit var regenerateAndUpdateLessonUseCase: RegenerateAndUpdateLessonUseCase

    @BeforeTest
    fun setUp() {
        deleteSections = mockk()
        fetchSections = mockk()
        generateLesson = mockk()
        updateLesson = mockk()
        regenerateAndUpdateLessonUseCase = RegenerateAndUpdateLessonUseCase(
            deleteSections,
            fetchSections,
            generateLesson,
            updateLesson
        )
    }

    @Test
    fun `invoke returns success when all operations succeed`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val originalLesson = dummyLesson

        // Simulate successful generation of lesson content
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = originalLesson.title,
            description = "New Description",
            content = listOf("New Content")
        )
        val updatedLesson = originalLesson.copy(
            title = generatedResponse.title,
            description = generatedResponse.description,
            content = generatedResponse.content
        )

        // Create dummy sections list
        val dummySections = listOf<Section>()  // Replace 'Any' with actual Section type if available

        coEvery { generateLesson(originalLesson.title, profile, curriculum, module) } returns flowOf(Result.success(generatedResponse))
        coEvery { fetchSections(profile.id, curriculum.id, module.id, originalLesson.id) } returns Result.success(dummySections)
        coEvery { deleteSections(dummySections, profile.id, curriculum.id, module.id, originalLesson.id) } returns Result.success(Unit)
        coEvery { updateLesson(any(), profile.id, curriculum.id, module.id) } returns Result.success(Unit)

        // Act
        val result = regenerateAndUpdateLessonUseCase(profile, curriculum, module, originalLesson)

        // Assert
        coVerify(exactly = 1) { generateLesson(originalLesson.title, profile, curriculum, module) }
        coVerify(exactly = 1) { fetchSections(profile.id, curriculum.id, module.id, originalLesson.id) }
        coVerify(exactly = 1) { deleteSections(dummySections, profile.id, curriculum.id, module.id, originalLesson.id) }
        coVerify(exactly = 1) { updateLesson(any(), profile.id, curriculum.id, module.id) }
        assertTrue(result.isSuccess)
        assertEquals(updatedLesson.title, result.getOrNull()?.title)
        assertEquals(updatedLesson.description, result.getOrNull()?.description)
        assertEquals(updatedLesson.content, result.getOrNull()?.content)
    }

    @Test
    fun `invoke returns failure when generation fails`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val originalLesson = dummyLesson

        val generationException = Exception("Generation failed")
        coEvery { generateLesson(originalLesson.title, profile, curriculum, module) } returns flowOf(Result.failure(generationException))

        // Act
        val result = regenerateAndUpdateLessonUseCase(profile, curriculum, module, originalLesson)

        // Assert
        coVerify(exactly = 1) { generateLesson(originalLesson.title, profile, curriculum, module) }
        // Subsequent steps should not be called on generation failure
        coVerify(exactly = 0) { fetchSections(any(), any(), any(), any()) }
        coVerify(exactly = 0) { deleteSections(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateLesson(any(), any(), any(), any()) }
        assertTrue(result.isFailure)
        assertEquals("Generation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when fetchSections fails`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val originalLesson = dummyLesson

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = originalLesson.title,
            description = "New Description",
            content = listOf("New Content")
        )
        coEvery { generateLesson(originalLesson.title, profile, curriculum, module) } returns flowOf(Result.success(generatedResponse))

        val fetchException = Exception("Fetch failed")
        coEvery { fetchSections(profile.id, curriculum.id, module.id, originalLesson.id) } returns Result.failure(fetchException)

        // Act
        val result = regenerateAndUpdateLessonUseCase(profile, curriculum, module, originalLesson)

        // Assert
        coVerify(exactly = 1) { generateLesson(originalLesson.title, profile, curriculum, module) }
        coVerify(exactly = 1) { fetchSections(profile.id, curriculum.id, module.id, originalLesson.id) }
        // Following steps should not proceed on fetch failure
        coVerify(exactly = 0) { deleteSections(any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { updateLesson(any(), any(), any(), any()) }
        assertTrue(result.isFailure)
        assertEquals("Fetch failed", result.exceptionOrNull()?.message)
    }

    // Additional tests for deleteSections or updateLesson failures can be added similarly

    // Dummy objects for testing purposes
    private val dummyProfile = Profile(
        username = "TestUser",
        email = "test@example.com",
        photoUrl = "https://example.com/photo.png",
        preferences = Profile.LearningPreferences(field = "COMPUTER_SCIENCE", level = "BEGINNER", goal = "Learn something")
    )

    private val dummyCurriculum = Curriculum(
        title = "Curriculum Title",
        description = "Curriculum Description",
        content = listOf("Topic 1", "Topic 2")
    )

    private val dummyModule = Module(
        title = "Module Title",
        description = "Module Description",
        content = listOf("Lesson 1", "Lesson 2")
    )

    private val dummyLesson = Lesson(
        title = "Original Lesson",
        description = "Original Description",
        content = listOf("Original Content")
    )
}
