package org.example.shared.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.*
import org.example.shared.data.local.dao.util.TimestampManager
import org.example.shared.data.local.dao.util.TimestampUpdater
import org.example.shared.data.local.database.LearnFlexDatabase
import org.example.shared.data.local.entity.*
import org.example.shared.domain.constant.Collection
import org.example.shared.domain.constant.Field
import org.example.shared.domain.constant.Level
import org.example.shared.domain.constant.Style
import org.example.shared.domain.model.Profile.*
import org.example.shared.domain.storage_operations.util.PathBuilder
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [28],
    manifest = Config.NONE
)
class DatabaseTest {
    private lateinit var database: LearnFlexDatabase
    private lateinit var profileDao: ProfileDao
    private lateinit var curriculumDao: CurriculumLocalDao
    private lateinit var moduleDao: ModuleLocalDao
    private lateinit var lessonDao: LessonLocalDao
    private lateinit var sectionDao: SectionLocalDao
    private lateinit var sessionDao: SessionLocalDao

    @Before
    fun setup() {
        // Start Koin for testing
        startKoin {
            modules(
                module {
                    // Create the updaters map
                    single<Map<Collection, TimestampUpdater>> {
                        mapOf(
                            Collection.PROFILES to database.profileTimestampUpdater(),
                            Collection.CURRICULA to database.curriculumTimestampUpdater(),
                            Collection.MODULES to database.moduleTimestampUpdater(),
                            Collection.LESSONS to database.lessonTimestampUpdater(),
                            Collection.SECTIONS to database.sectionTimestampUpdater(),
                            Collection.SESSIONS to database.sessionTimestampUpdater()
                        )
                    }

                    // Provide TimestampManager
                    single { TimestampManager(get()) }
                }
            )
        }

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LearnFlexDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        profileDao = database.profileDao()
        curriculumDao = database.curriculumDao()
        lessonDao = database.lessonDao()
        moduleDao = database.moduleDao()
        sectionDao = database.sectionDao()
        sessionDao = database.sessionDao()
    }

    @After
    fun cleanup() {
        if (::database.isInitialized) database.close()
        stopKoin()
    }

    @Test
    fun `when inserting profile, then profile should be retrievable`() = runTest {
        // When inserting a profile
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val retrieved = profileDao.get(userProfile.id).first()

        // Then profile should be stored correctly
        assertNotNull(retrieved)
        assertEquals(userProfile.username, retrieved.username)
    }

    @Test
    fun `when inserting curriculum, then curriculum should be retrievable and profile timestamp should update`() =
        runTest {
            // Given a profile in the database
            profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
            val initialProfile = profileDao.get(userProfile.id).first()
            assertNotNull(initialProfile)

            // When inserting a curriculum
            val path = PathBuilder().collection(Collection.PROFILES)
                .document(userProfile.id)
                .collection(Collection.CURRICULA)
                .document(curricula.first().id)
                .build()
            Thread.sleep(1) // Ensure timestamp will be different
            curriculumDao.insert(path, curricula.first(), System.currentTimeMillis())

            // Then curriculum should be stored correctly
            val retrievedCurriculum = curriculumDao.get(curricula.first().id).first()
            assertNotNull(retrievedCurriculum)
            assertEquals(curricula.first().title, retrievedCurriculum.title)

            // And profile timestamp should be updated
            val updatedProfile = profileDao.get(userProfile.id).first()
            assertNotNull(updatedProfile)
            assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
        }

    @Test
    fun `when inserting module, then module should be retrievable and parent timestamps should update`() = runTest {
        // Given a profile and curriculum in the database
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val initialProfile = profileDao.get(userProfile.id).first()
        assertNotNull(initialProfile)

        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(initialCurriculum)

        // When inserting a module
        Thread.sleep(1) // Ensure timestamp will be different
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())

        // Then module should be stored correctly
        val retrievedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(retrievedModule)
        assertEquals(modules.first().title, retrievedModule.title)

        // And parent timestamps should be updated
        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when inserting lesson, then lesson should be retrievable and parent timestamps should update`() = runTest {
        // Given a profile, curriculum, and module in the database
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val initialProfile = profileDao.get(userProfile.id).first()
        assertNotNull(initialProfile)

        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(initialCurriculum)

        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val initialModule = moduleDao.get(modules.first().id).first()
        assertNotNull(initialModule)

        // When inserting a lesson
        Thread.sleep(1) // Ensure timestamp will be different
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())

        // Then lesson should be stored correctly
        val retrievedLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(retrievedLesson)
        assertEquals(lessons.first().title, retrievedLesson.title)

        // And parent timestamps should be updated
        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when inserting section, then section should be retrievable and parent timestamps should update`() = runTest {
        // Given a profile, curriculum, module, and lesson in the database
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val initialProfile = profileDao.get(userProfile.id).first()
        assertNotNull(initialProfile)

        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(initialCurriculum)

        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val initialModule = moduleDao.get(modules.first().id).first()
        assertNotNull(initialModule)

        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())
        val initialLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(initialLesson)

        // When inserting a section
        Thread.sleep(1) // Ensure timestamp will be different
        val sectionPath = PathBuilder(lessonPath)
            .collection(Collection.SECTIONS)
            .document(sections.first().id)
            .build()
        sectionDao.insert(sectionPath, sections.first(), System.currentTimeMillis())

        // Then section should be stored correctly
        val retrievedSection = sectionDao.get(sections.first().id).first()
        assertNotNull(retrievedSection)
        assertEquals(sections.first().title, retrievedSection.title)

        // And parent timestamps should be updated
        val updatedLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson.lastUpdated > initialLesson.lastUpdated)

        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when updating curriculum description, then only curriculum and profile timestamps should update`() = runTest {
        // First, set up our initial state with a profile and curriculum
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)

        // Allow some time to pass to ensure distinct timestamps
        Thread.sleep(1)

        // Update curriculum description
        val updatedCurriculum = curricula.first().copy(
            description = "Updated curriculum description"
        )
        curriculumDao.update(curriculumPath, updatedCurriculum, System.currentTimeMillis())

        // Verify curriculum was updated properly
        val retrievedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(retrievedCurriculum)
        assertEquals("Updated curriculum description", retrievedCurriculum.description)
        assertTrue(retrievedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        // Verify profile timestamp was updated
        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when updating module title, then module and all parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        val initialModule = moduleDao.get(modules.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)
        assertNotNull(initialModule)

        Thread.sleep(1)

        // Update module title
        val updatedModule = modules.first().copy(
            title = "Updated module title"
        )
        moduleDao.update(modulePath, updatedModule, System.currentTimeMillis())

        // Verify module was updated properly
        val retrievedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(retrievedModule)
        assertEquals("Updated module title", retrievedModule.title)
        assertTrue(retrievedModule.lastUpdated > initialModule.lastUpdated)

        // Verify parent timestamps were updated in the correct order
        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when updating lesson content, then lesson and all parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        val initialModule = moduleDao.get(modules.first().id).first()
        val initialLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)
        assertNotNull(initialModule)
        assertNotNull(initialLesson)

        Thread.sleep(1)

        // Update lesson description
        val updatedLesson = lessons.first().copy(
            description = "Updated lesson description"
        )
        lessonDao.update(lessonPath, updatedLesson, System.currentTimeMillis())

        // Verify lesson was updated properly
        val retrievedLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(retrievedLesson)
        assertEquals("Updated lesson description", retrievedLesson.description)
        assertTrue(retrievedLesson.lastUpdated > initialLesson.lastUpdated)

        // Verify parent timestamps were updated in the correct order
        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when updating section content, then section and all parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())
        val sectionPath = PathBuilder(lessonPath)
            .collection(Collection.SECTIONS)
            .document(sections.first().id)
            .build()
        sectionDao.insert(sectionPath, sections.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        val initialModule = moduleDao.get(modules.first().id).first()
        val initialLesson = lessonDao.get(lessons.first().id).first()
        val initialSection = sectionDao.get(sections.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)
        assertNotNull(initialModule)
        assertNotNull(initialLesson)
        assertNotNull(initialSection)

        Thread.sleep(1)

        // Update section content
        val updatedSection = sections.first().copy(
            content = "Updated section content"
        )
        sectionDao.update(sectionPath, updatedSection, System.currentTimeMillis())

        // Verify section was updated properly
        val retrievedSection = sectionDao.get(sections.first().id).first()
        assertNotNull(retrievedSection)
        assertEquals("Updated section content", retrievedSection.content)
        assertTrue(retrievedSection.lastUpdated > initialSection.lastUpdated)

        // Verify parent timestamps were updated in the correct order
        val updatedLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson.lastUpdated > initialLesson.lastUpdated)

        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when deleting curriculum, then curriculum should be removed and profile timestamp should update`() = runTest {
        // First, set up our initial state with a profile and curriculum
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())

        // Capture the initial profile timestamp
        val initialProfile = profileDao.get(userProfile.id).first()
        assertNotNull(initialProfile)

        // Allow some time to pass to ensure distinct timestamps
        Thread.sleep(1)

        // Delete the curriculum
        curriculumDao.delete(curriculumPath, curricula.first(), System.currentTimeMillis())

        // Verify curriculum was deleted
        val retrievedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNull(retrievedCurriculum)

        // Verify profile timestamp was updated to reflect the deletion
        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when deleting module, then module should be removed and parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)

        Thread.sleep(1)

        // Delete the module
        moduleDao.delete(modulePath, modules.first(), System.currentTimeMillis())

        // Verify module was deleted
        val retrievedModule = moduleDao.get(modules.first().id).first()
        assertNull(retrievedModule)

        // Verify parent timestamps were updated to reflect the deletion
        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when deleting lesson, then lesson should be removed and parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        val initialModule = moduleDao.get(modules.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)
        assertNotNull(initialModule)

        Thread.sleep(1)

        // Delete the lesson
        lessonDao.delete(lessonPath, lessons.first(), System.currentTimeMillis())

        // Verify lesson was deleted
        val retrievedLesson = lessonDao.get(lessons.first().id).first()
        assertNull(retrievedLesson)

        // Verify parent timestamps were updated in the correct order
        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when deleting section, then section should be removed and parent timestamps should update`() = runTest {
        // Set up our initial hierarchy
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())
        val sectionPath = PathBuilder(lessonPath)
            .collection(Collection.SECTIONS)
            .document(sections.first().id)
            .build()
        sectionDao.insert(sectionPath, sections.first(), System.currentTimeMillis())

        // Capture initial timestamps
        val initialProfile = profileDao.get(userProfile.id).first()
        val initialCurriculum = curriculumDao.get(curricula.first().id).first()
        val initialModule = moduleDao.get(modules.first().id).first()
        val initialLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(initialProfile)
        assertNotNull(initialCurriculum)
        assertNotNull(initialModule)
        assertNotNull(initialLesson)

        Thread.sleep(1)

        // Delete the section
        sectionDao.delete(sectionPath, sections.first(), System.currentTimeMillis())

        // Verify section was deleted
        val retrievedSection = sectionDao.get(sections.first().id).first()
        assertNull(retrievedSection)

        // Verify parent timestamps were updated in the correct order
        val updatedLesson = lessonDao.get(lessons.first().id).first()
        assertNotNull(updatedLesson)
        assertTrue(updatedLesson.lastUpdated > initialLesson.lastUpdated)

        val updatedModule = moduleDao.get(modules.first().id).first()
        assertNotNull(updatedModule)
        assertTrue(updatedModule.lastUpdated > initialModule.lastUpdated)

        val updatedCurriculum = curriculumDao.get(curricula.first().id).first()
        assertNotNull(updatedCurriculum)
        assertTrue(updatedCurriculum.lastUpdated > initialCurriculum.lastUpdated)

        val updatedProfile = profileDao.get(userProfile.id).first()
        assertNotNull(updatedProfile)
        assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
    }

    @Test
    fun `when inserting multiple sessions, then all sessions should be stored and profile timestamp should update`() =
        runTest {
            // First, let's set up our initial state with just a profile
            profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
            val initialProfile = profileDao.get(userProfile.id).first()
            assertNotNull(initialProfile)

            // Create multiple sessions spanning different time periods
            val sessionsToInsert = listOf(
                sessions.first().copy(
                    id = "batch_session_1",
                    endTime = System.currentTimeMillis() + 3600000  // 1 hour session
                ),
                sessions.first().copy(
                    id = "batch_session_2",
                    endTime = System.currentTimeMillis() + 7200000  // 2 hour session
                ),
                sessions.first().copy(
                    id = "batch_session_3",
                    endTime = System.currentTimeMillis() + 10800000 // 3 hour session
                )
            )

            Thread.sleep(1) // Ensure distinct timestamps

            // Insert all sessions in a single transaction
            val sessionsPath = PathBuilder().collection(Collection.PROFILES)
                .document(userProfile.id)
                .collection(Collection.SESSIONS)
                .build()
            sessionDao.insertAll(sessionsPath, sessionsToInsert, System.currentTimeMillis())

            // Verify all sessions were stored correctly
            sessionsToInsert.forEach { session ->
                val retrievedSession = sessionDao.get(session.id).first()
                assertNotNull(retrievedSession)
                assertEquals(session.endTime, retrievedSession.endTime)
            }

            // Verify profile timestamp was updated
            val updatedProfile = profileDao.get(userProfile.id).first()
            assertNotNull(updatedProfile)
            assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
        }

    @Test
    fun `when updating multiple sessions end times, then all updates should be applied and profile timestamp should update`() =
        runTest {
            // Set up initial state with profile and sessions
            profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
            val sessionsPath = PathBuilder().collection(Collection.PROFILES)
                .document(userProfile.id)
                .collection(Collection.SESSIONS)
                .build()
            val initialSessions = listOf(
                sessions.first().copy(id = "update_session_1"),
                sessions.first().copy(id = "update_session_2"),
                sessions.first().copy(id = "update_session_3")
            )
            sessionDao.insertAll(sessionsPath, initialSessions, System.currentTimeMillis())

            val initialProfile = profileDao.get(userProfile.id).first()
            assertNotNull(initialProfile)

            // Create updated versions of all sessions with extended end times
            val updatedSessions = initialSessions.map { session ->
                session.copy(endTime = session.endTime + 1800000) // Extend each session by 30 minutes
            }

            Thread.sleep(1)

            // Update all sessions in a single transaction
            sessionDao.updateAll(sessionsPath, updatedSessions, System.currentTimeMillis())

            // Verify all sessions were updated correctly
            updatedSessions.forEach { session ->
                val retrievedSession = sessionDao.get(session.id).first()
                assertNotNull(retrievedSession)
                assertEquals(session.endTime, retrievedSession.endTime)
            }

            // Verify profile timestamp was updated exactly once despite multiple sessions being updated
            val updatedProfile = profileDao.get(userProfile.id).first()
            assertNotNull(updatedProfile)
            assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
        }

    @Test
    fun `when deleting multiple sessions, then all sessions should be removed and profile timestamp should update`() =
        runTest {
            // Set up initial state with profile and multiple sessions
            profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
            val sessionsPath = PathBuilder().collection(Collection.PROFILES)
                .document(userProfile.id)
                .collection(Collection.SESSIONS)
                .build()
            val sessionsToDelete = listOf(
                sessions.first().copy(id = "delete_session_1"),
                sessions.first().copy(id = "delete_session_2"),
                sessions.first().copy(id = "delete_session_3")
            )
            sessionDao.insertAll(sessionsPath, sessionsToDelete, System.currentTimeMillis())

            // Verify sessions were initially inserted
            sessionsToDelete.forEach { session ->
                val retrievedSession = sessionDao.get(session.id).first()
                assertNotNull(retrievedSession)
            }

            val initialProfile = profileDao.get(userProfile.id).first()
            assertNotNull(initialProfile)

            Thread.sleep(1)

            // Delete all sessions in a single transaction
            sessionDao.deleteAll(sessionsPath, sessionsToDelete, System.currentTimeMillis())

            // Verify all sessions were removed
            sessionsToDelete.forEach { session ->
                val retrievedSession = sessionDao.get(session.id).first()
                assertNull(retrievedSession)
            }

            // Verify profile timestamp was updated exactly once despite multiple deletions
            val updatedProfile = profileDao.get(userProfile.id).first()
            assertNotNull(updatedProfile)
            assertTrue(updatedProfile.lastUpdated > initialProfile.lastUpdated)
        }

    @Test
    fun `when inserting multiple sessions, then all sessions should be retrievable`() = runTest {
        // Given a profile in the database
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val initialProfile = profileDao.get(userProfile.id).first()
        assertNotNull(initialProfile)

        // Create multiple sessions to insert
        val sessionsToInsert = listOf(
            sessions.first().copy(id = "session_test_1"),
            sessions.first().copy(id = "session_test_2"),
            sessions.first().copy(id = "session_test_3")
        )

        // Insert all sessions
        sessionDao.insertAll(sessionsToInsert)

        // Verify all sessions were stored correctly
        sessionsToInsert.forEach { session ->
            val retrievedSession = sessionDao.get(session.id).first()
            assertNotNull(retrievedSession)
            assertEquals(session.endTime, retrievedSession.endTime)
        }
    }

    @Test
    fun `when getting lessons by curriculum ID, then correct lessons should be retrieved`() = runTest {
        // Given a profile, curriculum in the database, and mo
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())

        // Insert lessons associated with the curriculum
        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())
        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessons.forEach { lesson ->
            lessonDao.insert(lessonPath, lesson.copy(moduleId = modules.first().id), System.currentTimeMillis())
        }

        // When getting lessons by curriculum ID
        val retrievedLessons = lessonDao.getByCurriculumId(curricula.first().id).first()

        // Then the correct lessons should be retrieved
        assertNotNull(retrievedLessons)
        assertEquals(lessons.size, retrievedLessons.size)
        lessons.forEach { lesson ->
            assertTrue(retrievedLessons.any { it.id == lesson.id })
        }
    }

    @Test
    fun `when getting sections by curriculum ID, then correct sections should be retrieved`() = runTest {
        // Given a profile, curriculum, module, and lesson in the database
        profileDao.insert(PathBuilder().collection(Collection.PROFILES).build(), userProfile, System.currentTimeMillis())
        val curriculumPath = PathBuilder().collection(Collection.PROFILES)
            .document(userProfile.id)
            .collection(Collection.CURRICULA)
            .document(curricula.first().id)
            .build()
        curriculumDao.insert(curriculumPath, curricula.first(), System.currentTimeMillis())

        val modulePath = PathBuilder(curriculumPath)
            .collection(Collection.MODULES)
            .document(modules.first().id)
            .build()
        moduleDao.insert(modulePath, modules.first(), System.currentTimeMillis())

        val lessonPath = PathBuilder(modulePath)
            .collection(Collection.LESSONS)
            .document(lessons.first().id)
            .build()
        lessonDao.insert(lessonPath, lessons.first(), System.currentTimeMillis())

        // Insert sections associated with the lesson
        val sectionPath = PathBuilder(lessonPath)
            .collection(Collection.SECTIONS)
            .document(sections.first().id)
            .build()
        sections.forEach { section ->
            sectionDao.insert(sectionPath, section.copy(lessonId = lessons.first().id), System.currentTimeMillis())
        }

        // When getting sections by curriculum ID
        val retrievedSections = sectionDao.getByCurriculumId(curricula.first().id).first()

        // Then the correct sections should be retrieved
        assertNotNull(retrievedSections)
        assertEquals(sections.size, retrievedSections.size)
        sections.forEach { section ->
            assertTrue(retrievedSections.any { it.id == section.id })
        }
    }

    companion object {
        private val userProfile = ProfileEntity(
            id = "test_id",
            username = "test_user",
            email = "test@email.com",
            photoUrl = "test_photo.jpg",
            preferences = LearningPreferences(
                field = Field.LAW.name,
                level = Level.ADVANCED.name,
                goal = "test"
            ),
            learningStyle = LearningStyle(
                dominant = Style.READING.name,
                breakdown = LearningStyleBreakdown(
                    reading = 10,
                    kinesthetic = 5
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        private val curricula = listOf(
            CurriculumEntity(
                id = "curriculum_test_id",
                userId = userProfile.id,
                title = "Test title",
                description = "Test Description",
                content = listOf("Test Module 1", "Test Module 2"),
                status = "active",
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            CurriculumEntity(
                id = "curriculum_test_id_2",
                userId = userProfile.id,
                title = "Test title 2",
                description = "Test Description 2",
                content = listOf("Test Module 1", "Test Module 2"),
                status = "inactive",
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
        private val modules = listOf(
            ModuleEntity(
                id = "module_test_id_1",
                curriculumId = curricula.first().id,
                title = "Test Module 1",
                description = "Test Module Description 1",
                content = listOf("Test Lesson 1", "Test Lesson 2"),
                quizScore = 90,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            ModuleEntity(
                id = "module_test_id_2",
                curriculumId = curricula.last().id,
                title = "Test Module 2",
                description = "Test Module Description 2",
                content = listOf("Test Lesson 1", "Test Lesson 2"),
                quizScore = 85,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val lessons = listOf(
            LessonEntity(
                id = "lesson1",
                moduleId = modules.first().id,
                title = "Lesson 1 Title",
                description = "Lesson 1 Description",
                content = listOf("Test Section 1", "Test Section 2"),
                quizScore = 80,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            LessonEntity(
                id = "lesson2",
                moduleId = modules.last().id,
                title = "Lesson 2 Title",
                description = "Lesson 2 Description",
                content = listOf("Test Section 1", "Test Section 2"),
                quizScore = 85,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val sections = listOf(
            SectionEntity(
                id = "section1",
                lessonId = lessons.first().id,
                title = "Section 1 Title",
                description = "Section 1 Description",
                content = "Section 1 Content",
                quizScore = 85,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            SectionEntity(
                id = "section2",
                lessonId = lessons.last().id,
                title = "Section 2 Title",
                description = "Section 2 Description",
                content = "Section 2 Content",
                quizScore = 90,
                quizScoreMax = 100,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )

        private val sessions = listOf(
            SessionEntity(
                id = "session1",
                userId = userProfile.id,
                endTime = System.currentTimeMillis() + 3600000,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            ),
            SessionEntity(
                id = "session2",
                userId = userProfile.id,
                endTime = System.currentTimeMillis() + 7200000,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}