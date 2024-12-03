package org.example.shared.domain.use_case

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.example.shared.domain.model.Module
import org.example.shared.domain.repository.ModuleRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertTrue

class GetAllModulesUseCaseTest {
    private lateinit var useCase: GetAllModulesUseCase
    private lateinit var repository: ModuleRepository

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAllModulesUseCase(repository)
    }

    @Test
    fun `getAll should return modules flow when succeeds`() = runTest {
        val path = "test/path"
        val modules = listOf(mockk<Module>())
        val modulesFlow = flowOf(Result.success(modules))
        every { repository.getAll(path) } returns modulesFlow

        val result = useCase(path).single()

        verify(exactly = 1) { repository.getAll(path) }
        assertTrue { result.isSuccess }
        assertEquals(modules, result.getOrNull())
    }

    @Test
    fun `getAll should return error flow when fails`() = runTest {
        val path = "test/path"
        val exception = RuntimeException("Get failed")
        val errorFlow = flowOf(Result.failure<List<Module>>(exception))
        every { repository.getAll(path) } returns errorFlow

        val result = useCase(path).single()

        verify(exactly = 1) { repository.getAll(path) }
        assertTrue { result.isFailure }
        assertEquals(exception, result.exceptionOrNull())
    }
}