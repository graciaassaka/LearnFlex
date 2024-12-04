package org.example.shared.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.*
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.*
import org.example.shared.domain.model.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [28],
    manifest = Config.NONE
)
class DatabaseTest {
    private lateinit var database: LearnFlexDatabase
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var curriculumDao: CurriculumLocalDao
    private lateinit var moduleDao: ModuleLocalDao
    private lateinit var lessonDao: LessonLocalDao
    private lateinit var sectionDao: SectionLocalDao
    private lateinit var sessionDao: SessionLocalDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LearnFlexDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        userProfileDao = database.userProfileDao()
        curriculumDao = database.curriculumDao()
        lessonDao = database.lessonDao()
        moduleDao = database.moduleDao()
        sectionDao = database.sectionDao()
        sessionDao = database.sessionDao()
    }

    @After
    fun cleanup() {
        if (::database.isInitialized) database.close()
    }

    @Test
    fun insertAndRetrieveUserProfile() = runTest {
        // Then
        userProfileDao.insert(userProfile)
        val retrieved = userProfileDao.get(userProfile.id).first()

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved.username, userProfile.username)
    }

    @Test
    fun updateAndRetrieveUserProfile() = runTest {
        // Given
        val updatedProfile = userProfile.copy(
            username = "updated_user",
            email = "updated_email@email.com",
            photoUrl = "updated_photo.jpg",
        )

        // Then
        userProfileDao.insert(userProfile)
        userProfileDao.update(updatedProfile)
        val retrieved = userProfileDao.get(userProfile.id).first()

        // Assert
        assertNotNull(retrieved)
        assertNotEquals(retrieved.username, userProfile.username)
    }

    @Test
    fun deleteAndRetrieveUserProfile() = runTest {
        // Then
        userProfileDao.insert(userProfile)
        userProfileDao.delete(userProfile)
        val retrieved = userProfileDao.get(userProfile.id).first()

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun insertAndRetrieveCurriculumByStatus() = runTest {
        // Then
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        val activeRetrieved = curriculumDao.getCurriculaByStatus("active").first()
        val inactiveRetrieved = curriculumDao.getCurriculaByStatus("inactive").first()

        // Assert
        assertEquals(activeRetrieved, curricula.filter { it.status == "active" })
        assertEquals(inactiveRetrieved, curricula.filter { it.status == "inactive" })
    }

    @Test
    fun deleteAndRetrieveCurriculum() = runTest {
        // Then
        userProfileDao.insert(userProfile)
        curriculumDao.insert(curricula.first())
        curriculumDao.delete(curricula.first())
        val retrieved = curriculumDao.get(curricula.first().id)

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun updateAndRetrieveCurriculum() = runTest {
        // Given
        val updatedCurriculum = curricula.first().copy(
            id = "curriculum_test_id",
            imageUrl = "updated_image_url.jpg",
            syllabus = "Updated Syllabus",
            status = "inactive",
        )
        userProfileDao.insert(userProfile)

        // Then
        curriculumDao.insert(curricula.first())
        curriculumDao.update(updatedCurriculum)
        val retrieved = curriculumDao.get(curricula.first().id).first()

        // Assert
        assertNotNull(retrieved)
        assertNotEquals(retrieved.syllabus, curricula.first().syllabus)
    }

    @Test
    fun insertAndRetrieveModule() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insert(curricula.first())

        // Then
        moduleDao.insert(modules.first())
        val retrieved = moduleDao.get(modules.first().id).first()

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved, modules.first())
    }

    @Test
    fun getModulesByCurriculumId() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)

        // Then
        val retrieved = moduleDao.getModulesByCurriculumId(curricula.first().id).first()

        // Assert
        assertEquals(retrieved, modules.filter { it.curriculumId == curricula.first().id })
    }

    @Test
    fun getModuleIdsByMinQuizScore() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)

        // Then
        val retrieved = moduleDao.getModuleIdsByMinQuizScore(curricula.first().id, 90).first()

        // Assert
        assertEquals(
            retrieved,
            modules.filter { it.curriculumId == curricula.first().id && it.quizScore >= 90 }.map { it.id }
        )
    }

    @Test
    fun insertAndRetrieveLesson() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insert(curricula.first())
        moduleDao.insert(modules.first())

        // Then
        lessonDao.insert(lessons.first())
        val retrieved = lessonDao.get(lessons.first().id).first()

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved, lessons.first())
    }

    @Test
    fun getLessonsByModuleId() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)
        lessonDao.insertAll(lessons)

        // Then
        val retrieved = lessonDao.getLessonsByModuleId(modules.first().id).first()

        // Assert
        assertEquals(retrieved, lessons.filter { it.moduleId == modules.first().id })
    }

    @Test
    fun getLessonIdsByMinQuizScore() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)
        lessonDao.insertAll(lessons)

        // Then
        val retrieved = lessonDao.getLessonIdsByMinQuizScore(modules.first().id, 85).first()

        // Assert
        assertEquals(
            retrieved,
            lessons.filter { it.moduleId == modules.first().id && it.quizScore >= 85 }.map { it.id }
        )
    }

    @Test
    fun insertAndRetrieveSection() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insert(curricula.first())
        moduleDao.insert(modules.first())
        lessonDao.insert(lessons.first())

        // Then
        sectionDao.insert(sections.first())
        val retrieved = sectionDao.get(sections.first().id).first()

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved, sections.first())
    }

    @Test
    fun getSectionsByLessonId() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)
        lessonDao.insertAll(lessons)
        sectionDao.insertAll(sections)

        // Then
        val retrieved = sectionDao.getSectionsByLessonId(lessons.first().id).first()

        // Assert
        assertEquals(retrieved, sections.filter { it.lessonId == lessons.first().id })
    }

    @Test
    fun getSectionIdsByMinQuizScore() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)
        lessonDao.insertAll(lessons)
        sectionDao.insertAll(sections)

        // Then
        val retrieved = sectionDao.getSectionIdsByMinQuizScore(lessons.first().id, 85).first()

        // Assert
        assertEquals(
            retrieved,
            sections
                .filter { it.lessonId == lessons.first().id && it.quizScore >= 85 }
                .map { it.id }
        )
    }

    @Test
    fun insertAndRetrieveSession() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insert(curricula.first())
        moduleDao.insert(modules.first())
        lessonDao.insert(lessons.first())

        // Then
        sectionDao.insert(sections.first())
        sessionDao.insert(sessions.first())
        val retrieved = sessionDao.get(sessions.first().id).first()

        // Assert
        assertNotNull(retrieved)
        assertEquals(retrieved, sessions.first())
    }

    @Test
    fun getSessionsByDateRange() = runTest {
        // Given
        userProfileDao.insert(userProfile)
        curriculumDao.insertAll(curricula)
        moduleDao.insertAll(modules)
        lessonDao.insertAll(lessons)
        sessionDao.insertAll(sessions)

        // Then
        val start = System.currentTimeMillis()
        val end = start + 7200000
        val retrieved = sessionDao.getSessionsByDateRange(start, end).first()

        // Assert
        assertEquals(retrieved, sessions.filter { it.createdAt in start..end })
    }

    companion object {
        private val userProfile = UserProfileEntity(
            id = "test_id",
            username = "test_user",
            email = "test@email.com",
            photoUrl = "test_photo.jpg",
            preferences = LearningPreferences(
                field = Field.Law.name,
                level = Level.Advanced.name,
                goal = "test"
            ),
            learningStyle = LearningStyle(
                dominant = "Visual",
                breakdown = StyleBreakdown(
                    visual = 80,
                    reading = 10,
                    kinesthetic = 10
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        private val curricula = listOf(
            CurriculumEntity(
                id = "curriculum_test_id",
                userId = userProfile.id,
                imageUrl = "test_image_url.jpg",
                syllabus = "Test Syllabus",
                description = "Test Description",
                status = "active",
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            CurriculumEntity(
                id = "curriculum_test_id_2",
                userId = userProfile.id,
                imageUrl = "test_image_url_2.jpg",
                syllabus = "Test Syllabus 2",
                description = "Test Description 2",
                status = "inactive",
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
        private val modules = listOf(
            ModuleEntity(
                id = "module_test_id_1",
                curriculumId = curricula.first().id,
                imageUrl = "test_image_url_module_1.jpg",
                title = "Test Module 1",
                description = "Test Module Description 1",
                index = 1,
                quizScore = 90,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            ModuleEntity(
                id = "module_test_id_2",
                curriculumId = curricula.last().id,
                imageUrl = "test_image_url_module_2.jpg",
                title = "Test Module 2",
                description = "Test Module Description 2",
                index = 2,
                quizScore = 85,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val lessons = listOf(
            LessonEntity(
                id = "lesson1",
                moduleId = modules.first().id,
                imageUrl = "image1.jpg",
                title = "Lesson 1 Title",
                description = "Lesson 1 Description",
                index = 1,
                quizScore = 80,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            LessonEntity(
                id = "lesson2",
                moduleId = modules.last().id,
                imageUrl = "image2.jpg",
                title = "Lesson 2 Title",
                description = "Lesson 2 Description",
                index = 2,
                quizScore = 85,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val sections = listOf(
            SectionEntity(
                id = "section1",
                lessonId = lessons.first().id,
                imageUrl = "section_image1.jpg",
                index = 1,
                title = "Section 1 Title",
                description = "Section 1 Description",
                content = "Section 1 Content",
                quizScore = 85,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            SectionEntity(
                id = "section2",
                lessonId = lessons.last().id,
                imageUrl = "section_image2.jpg",
                index = 2,
                title = "Section 2 Title",
                description = "Section 2 Description",
                content = "Section 2 Content",
                quizScore = 90,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val sessions = listOf(
            SessionEntity(
                id = "session1",
                userId = lessons.first().id,
                endTime = System.currentTimeMillis() + 3600000,
                durationMinutes = 60,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            SessionEntity(
                id = "session2",
                userId = lessons.last().id,
                endTime = System.currentTimeMillis() + 7200000,
                durationMinutes = 120,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}