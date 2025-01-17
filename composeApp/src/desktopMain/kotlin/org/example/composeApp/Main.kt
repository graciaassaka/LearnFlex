package org.example.composeApp

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.app_name
import org.example.composeApp.injection.initKoin
import org.jetbrains.compose.resources.stringResource
import java.io.File

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun main() {
    println("Database location: ${File(System.getProperty("java.io.tmpdir"), "learnflex.db").absolutePath}")

    initKoin(null)

    application {
        Window(onCloseRequest = ::exitApplication, title = stringResource(Res.string.app_name)) {
            App(
                windowSizeClass = calculateWindowSizeClass(),
                cacheDir = File(System.getProperty("user.home"), ".learnflex/cache/images").apply { mkdirs() }
            )
        }
    }
}