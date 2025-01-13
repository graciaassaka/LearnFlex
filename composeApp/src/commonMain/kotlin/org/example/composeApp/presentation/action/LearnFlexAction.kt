package org.example.composeApp.presentation.action

/**
 * Represents actions that can be performed on the app viewmodel.
 */
sealed class LearnFlexAction {
    /**
     * Represents an action to open a specific curriculum within the LearnFlex screen.
     */
    data object Refresh : LearnFlexAction()
}