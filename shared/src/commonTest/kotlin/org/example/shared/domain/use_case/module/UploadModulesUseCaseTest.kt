package org.example.shared.domain.use_case.module

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test

class UploadModulesUseCaseTest {
    private lateinit var useCase: UploadModulesUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadModulesUseCase(repository)
    }

    @Test
    fun `upload should return success when succeeds`() = runTest {
        val path = "test/path"
        val module = listOf(mockk<Module>(relaxed = true))
        coEvery { repository.insertAll(any(), any(), any()) } returns Result.success(Unit)

        val result = useCase(path, module)

        coVerify(exactly = 1) { repository.insertAll(any(), any(), any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        val path = "test/path"
        val module = listOf(mockk<Module>(relaxed = true))
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insertAll(any(), any(), any()) } returns Result.failure(exception)

        val result = useCase(path, module)

        coVerify(exactly = 1) { repository.insertAll(any(), any(), any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}