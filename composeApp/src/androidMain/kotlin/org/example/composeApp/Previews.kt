package org.example.composeApp

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.composeApp.theme.LearnFlexTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=1920dp,height=1080dp,dpi=160"
)
@Composable
private fun DesktopDefaultPreview() {
    LearnFlexTheme {

    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "id:pixel_tablet"
)
@Composable
private fun TabletDefaultPreview() {
    LearnFlexTheme {
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(
    showSystemUi = true,
    showBackground = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun PhoneDefaultPreview() {
    LearnFlexTheme {

    }
}

