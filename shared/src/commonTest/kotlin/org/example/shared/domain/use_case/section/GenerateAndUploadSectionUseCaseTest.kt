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

class GenerateAndUploadSectionUseCaseTest {

    private lateinit var generateSection: GenerateSectionUseCase
    private lateinit var uploadSectionUseCase: UploadSectionUseCase
    private lateinit var generateAndUploadSectionUseCase: GenerateAndUploadSectionUseCase

    @BeforeTest
    fun setUp() {
        generateSection = mockk()
        uploadSectionUseCase = mockk()
        generateAndUploadSectionUseCase = GenerateAndUploadSectionUseCase(generateSection, uploadSectionUseCase)
    }

    @Test
    fun `invoke returns success when generation and upload succeed`() = runTest {
        // Arrange
        val title = "Section Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson

        // Simulate generated response from generateSection
        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "Generated Description",
            content = listOf("Content 1", "Content 2")
        )
        // Expected Section after generation
        val expectedSection = Section(
            title = title,
            description = generatedResponse.description,
            content = generatedResponse.content
        )

        // Stub generateSection to return a successful flow
        coEvery {
            generateSection(title, profile, curriculum, module, lesson)
        } returns flowOf(Result.success(generatedResponse))

        // Stub uploadSectionUseCase to succeed
        coEvery {
            uploadSectionUseCase(any<Section>(), profile.id, curriculum.id, module.id, lesson.id)
        } returns Result.success(Unit)

        // Act
        val result = generateAndUploadSectionUseCase(title, profile, curriculum, module, lesson)

        // Assert
        coVerify(exactly = 1) { generateSection(title, profile, curriculum, module, lesson) }
        coVerify(exactly = 1) {
            uploadSectionUseCase(any<Section>(), profile.id, curriculum.id, module.id, lesson.id)
        }
        assertTrue(result.isSuccess)
        val sectionResult = result.getOrNull()
        assertEquals(expectedSection.title, sectionResult?.title)
        assertEquals(expectedSection.description, sectionResult?.description)
        assertEquals(expectedSection.content, sectionResult?.content)
    }

    @Test
    fun `invoke returns failure when generation fails`() = runTest {
        // Arrange
        val title = "Section Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson

        val generationException = Exception("Generation failed")
        coEvery {
            generateSection(title, profile, curriculum, module, lesson)
        } returns flowOf(Result.failure(generationException))

        // Act
        val result = generateAndUploadSectionUseCase(title, profile, curriculum, module, lesson)

        // Assert
        coVerify(exactly = 1) { generateSection(title, profile, curriculum, module, lesson) }
        // uploadSectionUseCase should not be called if generation fails
        coVerify(exactly = 0) { uploadSectionUseCase(any(), any(), any(), any(), any()) }
        assertTrue(result.isFailure)
        assertEquals("Generation failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when upload fails`() = runTest {
        // Arrange
        val title = "Section Title"
        val profile = dummyProfile
        val curriculum = dummyCurriculum
        val module = dummyModule
        val lesson = dummyLesson

        val generatedResponse = ContentGeneratorClient.GeneratedResponse(
            title = title,
            description = "Generated Description",
            content = listOf("Content 1", "Content 2")
        )

        coEvery {
            generateSection(title, profile, curriculum, module, lesson)
        } returns flowOf(Result.success(generatedResponse))

        val uploadException = Exception("Upload failed")
        coEvery {
            uploadSectionUseCase(any<Section>(), profile.id, curriculum.id, module.id, lesson.id)
        } returns Result.failure(uploadException)

        // Act
        val result = generateAndUploadSectionUseCase(title, profile, curriculum, module, lesson)

        // Assert
        coVerify(exactly = 1) { generateSection(title, profile, curriculum, module, lesson) }
        coVerify(exactly = 1) {
            uploadSectionUseCase(any<Section>(), profile.id, curriculum.id, module.id, lesson.id)
        }
        assertTrue(result.isFailure)
        assertEquals("Upload failed", result.exceptionOrNull()?.message)
    }

    // Dummy objects for testing
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
        title = "Lesson Title",
        description = "Lesson Description",
        content = listOf("Content A", "Content B")
    )
}
