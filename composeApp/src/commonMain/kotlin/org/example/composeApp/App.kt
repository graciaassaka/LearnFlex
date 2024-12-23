package org.example.composeApp

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import org.example.composeApp.navigation.Navigator
import org.example.composeApp.theme.LearnFlexTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
@Preview
fun App(windowSizeClass: WindowSizeClass, cacheDir: File) {
    val navHostController = rememberNavController()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    CompositionLocalProvider {
        LearnFlexTheme {
            Navigator(
                navController = navHostController,
                windowSizeClass = windowSizeClass
            )
        }
    }
}