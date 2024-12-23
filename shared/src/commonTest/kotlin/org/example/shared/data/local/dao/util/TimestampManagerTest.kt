package org.example.shared.data.local.dao.util

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.util.TimestampUpdater.*
import org.example.shared.domain.constant.DataCollection
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TimestampManagerTest {
    private lateinit var profileUpdater: ProfileTimestampUpdater
    private lateinit var curriculumUpdater: CurriculumTimestampUpdater
    private lateinit var moduleUpdater: ModuleTimestampUpdater
    private lateinit var lessonUpdater: LessonTimestampUpdater
    private lateinit var sectionUpdater: SectionTimestampUpdater
    private lateinit var sessionUpdater: SessionTimestampUpdater
    private lateinit var timestampManager: TimestampManager

    @Before
    fun setup() {
        // Initialize mock updaters
        profileUpdater = mockk(relaxed = true)
        curriculumUpdater = mockk(relaxed = true)
        moduleUpdater = mockk(relaxed = true)
        lessonUpdater = mockk(relaxed = true)
        sectionUpdater = mockk(relaxed = true)
        sessionUpdater = mockk(relaxed = true)

        // Create TimestampManager with mock updaters
        timestampManager = TimestampManager(
            mapOf(
                DataCollection.PROFILES to profileUpdater,
                DataCollection.CURRICULA to curriculumUpdater,
                DataCollection.MODULES to moduleUpdater,
                DataCollection.LESSONS to lessonUpdater,
                DataCollection.SECTIONS to sectionUpdater,
                DataCollection.SESSIONS to sessionUpdater
            )
        )
    }


    @Test
    fun `should update profile timestamp`() = runTest {
        // Given
        val path = "profiles/profile123"
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify { profileUpdater.updateTimestamp("profile123", timestamp) }
    }

    @Test
    fun `should update curriculum timestamp`() = runTest {
        // Given
        val path = "curricula/curr123"
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify { curriculumUpdater.updateTimestamp("curr123", timestamp) }
    }

    @Test
    fun `should update nested module timestamp`() = runTest {
        // Given
        val path = "curricula/curr123/modules/module123"
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            moduleUpdater.updateTimestamp("module123", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
        }
    }

    @Test
    fun `should update deeply nested section timestamp`() = runTest {
        // Given
        val path = "curricula/curr123/modules/module123/lessons/lesson123/sections/section123"
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            sectionUpdater.updateTimestamp("section123", timestamp)
            lessonUpdater.updateTimestamp("lesson123", timestamp)
            moduleUpdater.updateTimestamp("module123", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
        }
    }

    @Test
    fun `should handle invalid collection name gracefully`() = runTest {
        // Given
        val path = "invalid/123/modules/module123"
        val timestamp = 1234567890L

        // When
        assertFailsWith<IllegalArgumentException> {
            timestampManager.updateTimestamps(path, timestamp)
        }
    }

    @Test
    fun `should handle empty path`() = runTest {
        // Given
        val path = ""
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify(exactly = 0) {
            profileUpdater.updateTimestamp(any(), any())
            curriculumUpdater.updateTimestamp(any(), any())
            moduleUpdater.updateTimestamp(any(), any())
            lessonUpdater.updateTimestamp(any(), any())
            sectionUpdater.updateTimestamp(any(), any())
            sessionUpdater.updateTimestamp(any(), any())
        }
    }

    @Test
    fun `should handle single segment path`() = runTest {
        // Given
        val path = "profiles"
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify(exactly = 0) {
            profileUpdater.updateTimestamp(any(), any())
            curriculumUpdater.updateTimestamp(any(), any())
            moduleUpdater.updateTimestamp(any(), any())
            lessonUpdater.updateTimestamp(any(), any())
            sectionUpdater.updateTimestamp(any(), any())
            sessionUpdater.updateTimestamp(any(), any())
        }
    }
}