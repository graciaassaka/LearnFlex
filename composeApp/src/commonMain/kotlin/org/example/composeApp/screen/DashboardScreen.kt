package org.example.composeApp.screen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun DashboardScreen() {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data("https://th.bing.com/th/id/OIP.-d3mzckq9KcyCelJj_6oJgHaD1?rs=1&pid=ImgDetMain")
            .crossfade(true)
            .build(),
        imageLoader = SingletonImageLoader.get(LocalPlatformContext.current)
    )

    Image(
        painter = painter,
        contentDescription = "Dashboard Image",
    )
}