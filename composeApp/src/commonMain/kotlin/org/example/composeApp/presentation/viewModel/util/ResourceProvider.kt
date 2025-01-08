package org.example.composeApp.presentation.viewModel.util

import org.jetbrains.compose.resources.StringResource

/**
 * A functional interface for providing string resources.
 */
fun interface ResourceProvider {
    /**
     * Suspends the function to get a string resource.
     *
     * @param res The string resource to retrieve.
     * @return The string associated with the provided resource.
     */
    suspend fun getString(res: StringResource): String
}