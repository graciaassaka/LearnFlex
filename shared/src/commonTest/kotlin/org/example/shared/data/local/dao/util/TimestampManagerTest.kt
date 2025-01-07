package org.example.shared.data.local.dao.util

import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.util.TimestampUpdater.*
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.Before
import kotlin.test.Test

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
                Collection.PROFILES to profileUpdater,
                Collection.CURRICULA to curriculumUpdater,
                Collection.MODULES to moduleUpdater,
                Collection.LESSONS to lessonUpdater,
                Collection.SECTIONS to sectionUpdater,
                Collection.SESSIONS to sessionUpdater
            )
        )
    }


    @Test
    fun `should update profile timestamp`() = runTest {
        // Given
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document("userId")
            .build()
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify { profileUpdater.updateTimestamp("userId", timestamp) }
    }

    @Test
    fun `should update curriculum timestamp`() = runTest {
        // Given
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document("userId")
            .collection(Collection.CURRICULA)
            .document("curr123")
            .build()
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            profileUpdater.updateTimestamp("userId", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
        }
    }

    @Test
    fun `should update nested module timestamp`() = runTest {
        // Given
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document("userId")
            .collection(Collection.CURRICULA)
            .document("curr123")
            .collection(Collection.MODULES)
            .document("module123")
            .build()
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            profileUpdater.updateTimestamp("userId", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
            moduleUpdater.updateTimestamp("module123", timestamp)
        }
    }

    @Test
    fun `should update lesson timestamp`() = runTest {
        // Given
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document("userId")
            .collection(Collection.CURRICULA)
            .document("curr123")
            .collection(Collection.MODULES)
            .document("module123")
            .collection(Collection.LESSONS)
            .document("lesson123")
            .build()
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            profileUpdater.updateTimestamp("userId", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
            moduleUpdater.updateTimestamp("module123", timestamp)
            lessonUpdater.updateTimestamp("lesson123", timestamp)
        }
    }

    @Test
    fun `should update deeply nested section timestamp`() = runTest {
        // Given
        val path = PathBuilder()
            .collection(Collection.PROFILES)
            .document("userId")
            .collection(Collection.CURRICULA)
            .document("curr123")
            .collection(Collection.MODULES)
            .document("module123")
            .collection(Collection.LESSONS)
            .document("lesson123")
            .collection(Collection.SECTIONS)
            .document("section123")
            .build()
        val timestamp = 1234567890L

        // When
        timestampManager.updateTimestamps(path, timestamp)

        // Then
        coVerify {
            profileUpdater.updateTimestamp("userId", timestamp)
            curriculumUpdater.updateTimestamp("curr123", timestamp)
            moduleUpdater.updateTimestamp("module123", timestamp)
            lessonUpdater.updateTimestamp("lesson123", timestamp)
            sectionUpdater.updateTimestamp("section123", timestamp)
        }
    }
}