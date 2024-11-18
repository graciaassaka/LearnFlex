package org.example.shared.data.sync.manager

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.example.shared.domain.constant.SyncOperationType
import org.example.shared.domain.constant.SyncStatus
import org.example.shared.domain.sync.SyncHandler
import org.example.shared.domain.sync.SyncOperation
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerImplTest {
    private lateinit var testScope: TestScope
    private lateinit var mockSyncHandler: MockSyncHandler
    private lateinit var syncManager: SyncManagerImpl<String>

    @BeforeTest
    fun setup() {
        testScope = TestScope()
        mockSyncHandler = MockSyncHandler()
    }

    @Test
    fun `successful sync operation updates status correctly`() = testScope.runTest {
        // Given
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // When
        syncManager.queueOperation(operation)

        // Then
        withTimeout(5.seconds) {
            while (syncManager.syncStatus.value !is SyncStatus.Success) {
                advanceTimeBy(100)
            }
        }

        assertEquals(SyncStatus.Success, syncManager.syncStatus.value)
        assertTrue(mockSyncHandler.operations.contains(operation))
    }

    @Test
    fun `failed sync operation with retries updates status correctly`() = testScope.runTest {
        // Given
        mockSyncHandler.shouldFail = true
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // When
        syncManager.queueOperation(operation)

        // Then
        withTimeout(5.seconds) {
            while (syncManager.syncStatus.value !is SyncStatus.Error) {
                advanceTimeBy(1000)
            }
        }

        assertTrue(syncManager.syncStatus.value is SyncStatus.Error)
        assertEquals(4, mockSyncHandler.callCount) // Initial attempt + 3 retries
    }

    @Test
    fun `multiple operations are processed sequentially`() = testScope.runTest {
        // Given
        val operations = List(3) { index ->
            SyncOperation(SyncOperationType.CREATE, "test data $index")
        }
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // When
        operations.forEach { syncManager.queueOperation(it) }

        // Then
        withTimeout(5.seconds) {
            while (mockSyncHandler.operations.size < operations.size) {
                advanceTimeBy(100)
            }
        }

        assertEquals(operations.size, mockSyncHandler.operations.size)
    }

    @Test
    fun `sync status shows in-progress during operation`() = testScope.runTest {
        // Given
        mockSyncHandler.addDelay = true
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // When
        syncManager.queueOperation(operation)
        advanceTimeBy(100)

        // Then
        assertEquals(SyncStatus.InProgress, syncManager.syncStatus.value)

        // Complete the operation
        withTimeout(5.seconds) {
            while (syncManager.syncStatus.value !is SyncStatus.Success) {
                advanceTimeBy(100)
            }
        }
    }

    @Test
    fun `retry mechanism works with eventual success`() = testScope.runTest {
        // Given
        mockSyncHandler.failCount = 2 // Fail twice then succeed
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // When
        syncManager.queueOperation(operation)

        // Then
        withTimeout(5.seconds) {
            while (syncManager.syncStatus.value !is SyncStatus.Success) {
                advanceTimeBy(1000)
            }
        }

        assertEquals(SyncStatus.Success, syncManager.syncStatus.value)
        assertEquals(3, mockSyncHandler.callCount) // 2 failures + 1 success
    }

    @Test
    fun `close cancels job and stops processing operations`() = testScope.runTest {
        // Given
        mockSyncHandler.addDelay = true
        syncManager = SyncManagerImpl(
            syncScope = backgroundScope,
            syncHandler = mockSyncHandler
        )

        // Start an operation that will take some time
        syncManager.queueOperation(operation)
        advanceTimeBy(100) // Let the operation start

        // When
        syncManager.close()
        advanceTimeBy(1000) // Give enough time for the operation to complete if it wasn't cancelled

        // Then
        assertEquals(SyncStatus.InProgress, syncManager.syncStatus.value) // Status should remain in progress
        assertEquals(0, mockSyncHandler.operations.size) // Operation should not complete
    }

    companion object {
        private val operation = SyncOperation(SyncOperationType.CREATE, "test data")
    }
}

private class MockSyncHandler : SyncHandler<String> {
    val operations = mutableListOf<SyncOperation<String>>()
    var shouldFail = false
    var failCount = 0
    var callCount = 0
    var addDelay = false

    override suspend fun handleSync(operation: SyncOperation<String>) {
        callCount++
        if (addDelay) delay(1000)

        when {
            shouldFail -> throw Exception("Sync failed")
            failCount > 0 -> {
                failCount--
                throw Exception("Temporary failure")
            }
            else -> operations.add(operation)
        }
    }
}