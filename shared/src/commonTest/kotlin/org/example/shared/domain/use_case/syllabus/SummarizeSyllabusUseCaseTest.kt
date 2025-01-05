package org.example.shared.domain.use_case.syllabus

import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.client.SyllabusSummarizerClient
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SummarizeSyllabusUseCaseTest {
    private lateinit var syllabusSummarizerClient: SyllabusSummarizerClient
    private lateinit var summarizeSyllabusUseCase: SummarizeSyllabusUseCase

    @Before
    fun setUp() {
        syllabusSummarizerClient = mockk(relaxed = true)
        summarizeSyllabusUseCase = SummarizeSyllabusUseCase(syllabusSummarizerClient)
    }

    @Test
    fun `invoke should call summarizeSyllabus`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")

        val description = "Summary of the syllabus"


        // Mock summarizeSyllabus to return a successful summary
        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.success(description))

        // Act
        val result = summarizeSyllabusUseCase.invoke(syllabusFile).first()

        // Assert
        coVerify(exactly = 1) {
            syllabusSummarizerClient.summarizeSyllabus(syllabusFile)
        }

        // Verify that the result is successful and matches the generated curriculum
        assertTrue(result.isSuccess)
        assertEquals(description, result.getOrNull())
    }


    @Test
    fun `invoke should return success when summarizeSyllabus succeeds`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")
        val description = "Summary of the syllabus"

        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.success(description))

        // Act
        val result = summarizeSyllabusUseCase.invoke(syllabusFile).first()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(description, result.getOrNull())
    }

    /**
     * Tests that the use case throws an IllegalArgumentException when the file does not exist.
     */
    @Test
    fun `invoke should throw IllegalArgumentException when file does not exist`() = runTest {
        // Arrange
        val syllabusFile = mockk<File>(relaxed = true) {
            every { exists() } returns false
        }

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            summarizeSyllabusUseCase.invoke(syllabusFile).first().getOrThrow()
        }
        assertEquals("File does not exist", exception.message)

        // Verify that summarizeSyllabus and generateCurriculumFromDescriptionUseCase are never called
        coVerifyAll(inverse = true) {
            syllabusSummarizerClient.summarizeSyllabus(any())
        }
    }

    /**
     * Tests that the use case throws an IllegalArgumentException when the file has an unsupported extension.
     */
    @Test
    fun `invoke should throw IllegalArgumentException when file has unsupported extension`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".txt")

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            summarizeSyllabusUseCase.invoke(syllabusFile).first().getOrThrow()
        }
        assertEquals("File must be a PDF or DOCX", exception.message)

        // Verify that summarizeSyllabus and generateCurriculumFromDescriptionUseCase are never called
        coVerifyAll(inverse = true) {
            syllabusSummarizerClient.summarizeSyllabus(any())
        }
    }

    /**
     * Tests that the use case returns a failure result when summarizeSyllabus fails.
     */
    @Test
    fun `invoke should return failure when summarizeSyllabus fails`() = runTest {
        // Arrange
        val syllabusFile = File.createTempFile("syllabus", ".pdf")

        val exception = Exception("Summarization failed")

        coEvery { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) } returns flowOf(Result.failure(exception))

        // Act
        val result = summarizeSyllabusUseCase.invoke(syllabusFile).first()

        // Assert
        assertTrue(result.isFailure)
        val thrownException = result.exceptionOrNull()
        assertIs<Exception>(thrownException)
        assertEquals("Summarization failed", thrownException.message)

        // Verify that generateCurriculumFromDescriptionUseCase is never called
        coVerify(exactly = 1) { syllabusSummarizerClient.summarizeSyllabus(syllabusFile) }
    }
}