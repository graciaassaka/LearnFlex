package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.ContentGeneratorClient
import org.example.shared.domain.model.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegenerateAndUpdateSectionUseCaseTest {

    private lateinit var generateSection: GenerateSectionUseCase
    private lateinit var updateSection: UploadSectionUseCase
    private lateinit var regenerateAndUpdateSectionUseCase: RegenerateAndUpdateSectionUseCase

    @BeforeTest
    fun setUp() {
        generateSection = mockk()
        updateSection = mockk()
        regenerateAndUpdateSectionUseCase = RegenerateAndUpdateSectionUseCase(generateSection, updateSection)
    }

    @Test
    fun `invoke returns success when generation and update succeed`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson
        val section = dummySection

        // Simulate successful generation of section content
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = section.title,
            description = "Updated Description",
            content = listOf("Updated Content")
        )
        // The use case uses the original section for updating, so we expect that
        // rather than using an updated copy
        coEvery {
            generateSection(section.title, profile, curriculum, module, lesson)
        } returns flowOf(Result.success(generatedResponse))

        coEvery {
            updateSection(any(), profile.id, curriculum.id, module.id, lesson.id)
        } returns Result.success(Unit)

        // Act
        val result = regenerateAndUpdateSectionUseCase(profile, curriculum, module, lesson, section)

        // Assert
        coVerify(exactly = 1) {
            generateSection(section.title, profile, curriculum, module, lesson)
        }
        coVerify(exactly = 1) {
            updateSection(any(), profile.id, curriculum.id, module.id, lesson.id)
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when generation fails`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson
        val section = dummySection

        val generationException = Exception("Generation failed")
        coEvery {
            generateSection(section.title, profile, curriculum, module, lesson)
        } returns flowOf(Result.failure(generationException))

        // Act
        val result = regenerateAndUpdateSectionUseCase(profile, curriculum, module, lesson, section)

        // Assert
        coVerify(exactly = 1) {
            generateSection(section.title, profile, curriculum, module, lesson)
        }
        coVerify(exactly = 0) {
            updateSection(any(), any(), any(), any(), any())
        }
        assertTrue(result.isFailure)
        assertEquals("Generation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when update fails`() = runTest {
        // Arrange
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson
        val section = dummySection

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = section.title,
            description = "Updated Description",
            content = listOf("Updated Content")
        )

        coEvery {
            generateSection(section.title, profile, curriculum, module, lesson)
        } returns flowOf(Result.success(generatedResponse))

        val updateException = Exception("Update failed")
        coEvery {
            updateSection(any(), profile.id, curriculum.id, module.id, lesson.id)
        } returns Result.failure(updateException)

        // Act
        val result = regenerateAndUpdateSectionUseCase(profile, curriculum, module, lesson, section)

        // Assert
        coVerify(exactly = 1) {
            generateSection(section.title, profile, curriculum, module, lesson)
        }
        coVerify(exactly = 1) {
            updateSection(any(), profile.id, curriculum.id, module.id, lesson.id)
        }
        assertTrue(result.isFailure)
        assertEquals("Update failed", result.exceptionOrNull()?.message)
    }

    // Dummy objects for testing
    private val dummyProfile = Profile(
        username = "TestUser",
        email = "test@example.com",
        photoUrl = "http://example.com/photo.png",
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
        title = "Lesson Title",
        description = "Lesson Description",
        content = listOf("Content A", "Content B")
    )

    private val dummySection = Section(
        title = "Section Title",
        description = "Section Description",
        content = listOf("Initial Content")
    )
}
