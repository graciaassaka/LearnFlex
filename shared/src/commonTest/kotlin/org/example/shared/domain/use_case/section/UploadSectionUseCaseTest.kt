package org.example.shared.domain.use_case.section

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Section
import org.example.shared.domain.repository.SectionRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class UploadSectionUseCaseTest {
    private lateinit var useCase: UploadSectionUseCase
    private lateinit var repository: SectionRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UploadSectionUseCase(repository)
    }

    @Test
    fun `upload should return success when succeeds`() = runTest {
        val path = "test/path"
        val section = mockk<Section>(relaxed = true)
        coEvery { repository.insert(path, section, any()) } returns Result.success(Unit)

        val result = useCase(path, section)

        coVerify(exactly = 1) { repository.insert(path, section, any()) }
        assert(result.isSuccess)
    }

    @Test
    fun `upload should return failure when fails`() = runTest {
        val path = "test/path"
        val section = mockk<Section>(relaxed = true)
        val exception = RuntimeException("Upload failed")
        coEvery { repository.insert(path, section, any()) } returns Result.failure(exception)

        val result = useCase(path, section)

        coVerify(exactly = 1) { repository.insert(path, section, any()) }
        assert(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}