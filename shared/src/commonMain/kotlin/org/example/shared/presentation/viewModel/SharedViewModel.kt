package org.example.shared.presentation.viewModel

expect class SharedViewModel
{
    suspend fun getUserData()
    fun clearError()
}
