package org.example.composeApp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.shared.injection.initKoin

/**
 * The main entry point of the application.
 */
fun main() {
    initKoin(null)
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "LearnFlex",
        ) {
            App()
        }
    }
}