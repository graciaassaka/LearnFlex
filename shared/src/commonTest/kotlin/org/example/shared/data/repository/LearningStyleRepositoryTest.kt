package org.example.shared.data.repository

import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.data.local.dao.LearningStyleDao
import org.example.shared.data.local.entity.LearningStyleEntity
import org.example.shared.domain.data_source.RemoteDataSource
import org.example.shared.domain.model.LearningStyle
import org.example.shared.domain.model.StyleBreakdown
import org.example.shared.domain.model.StyleResult
import org.example.shared.domain.repository.Repository
import org.example.shared.domain.sync.SyncManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotEquals

class LearningStyleRepositoryTest {
    private lateinit var repository: Repository<LearningStyle>
    private lateinit var learningStyleDao: LearningStyleDao
    private lateinit var remoteDataSource: RemoteDataSource<LearningStyle>
    private lateinit var syncManager: SyncManager<LearningStyle>

    @Before
    fun setUp() {
        learningStyleDao = mockk()
        remoteDataSource = mockk()
        syncManager = mockk()
        repository = LearningStyleRepository(remoteDataSource, learningStyleDao, syncManager)
    }

    @Test
    fun `create should insert the learning style and queue a create operation`() = runTest {
        // Given
        coEvery { learningStyleDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.create(learningStyle)

        // Then
        coVerify { learningStyleDao.insert(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `update should update the learning style and queue an update operation`() = runTest {
        // Given
        coEvery { learningStyleDao.update(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.update(learningStyle)

        // Then
        coVerify { learningStyleDao.update(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    @Test
    fun `get should return the learning style from the local database`() = runTest {
        // Given
        coEvery { learningStyleDao.get(any()) } returns with(learningStyle) {
            LearningStyleEntity(id, style, createdAt, lastUpdated)
        }
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = repository.get(learningStyle.id).single()

        // Then
        coVerify { learningStyleDao.get(any()) }
        coVerify { syncManager.queueOperation(any()) }
        assertTrue(result.isSuccess)
        assertEquals(learningStyle.id, result.getOrNull()?.id)
        assertEquals(learningStyle.style, result.getOrNull()?.style)
    }

    @Test
    fun `get should return the learning style from the remote data source when local is null`() = runTest {
        // Given
        val updatedStyle = LearningStyle(
            id = "testId",
            style = StyleResult(
                dominantStyle = "Reading",
                StyleBreakdown(
                    visual = 10,
                    reading = 15,
                    kinesthetic = 10
                )
            ),
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
        coEvery { learningStyleDao.get(any()) } returns null
        coEvery { remoteDataSource.fetch(updatedStyle.id) } returns Result.success(updatedStyle)
        coEvery { learningStyleDao.insert(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        val result = repository.get(updatedStyle.id).single()

        // Then
        coVerify { learningStyleDao.get(any()) }
        coVerify { remoteDataSource.fetch(updatedStyle.id) }
        coVerify { learningStyleDao.insert(any()) }
        assertTrue(result.isSuccess)
        assertEquals(updatedStyle.id, result.getOrNull()?.id)
        assertNotEquals(learningStyle.style, result.getOrNull()?.style)
    }

    @Test
    fun `get should return an error if the learning style cannot be retrieved`() = runTest {
        // Given
        coEvery { learningStyleDao.get(any()) } returns null
        coEvery { remoteDataSource.fetch(learningStyle.id) } throws Exception("Test exception")

        // When
        val result = repository.get(learningStyle.id).single()

        // Then
        coVerify { learningStyleDao.get(any()) }
        coVerify { remoteDataSource.fetch(learningStyle.id) }
        assertTrue(result.isFailure)
        assertEquals("Test exception", result.exceptionOrNull()?.message)
    }

    @Test
    fun `delete should delete the learning style and queue a delete operation`() = runTest {
        // Given
        coEvery { learningStyleDao.delete(any()) } just runs
        coEvery { syncManager.queueOperation(any()) } just runs

        // When
        repository.delete(learningStyle)

        // Then
        coVerify { learningStyleDao.delete(any()) }
        coVerify { syncManager.queueOperation(any()) }
    }

    companion object {
        private val styleResult = StyleResult(
            dominantStyle = "Visual",
            StyleBreakdown(
                visual = 10,
                reading = 10,
                kinesthetic = 10
            )
        )
        private val learningStyle = LearningStyle(
            id = "testId",
            style = styleResult,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )
    }
}