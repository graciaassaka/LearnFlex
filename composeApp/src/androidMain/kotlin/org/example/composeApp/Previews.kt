package org.example.composeApp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.composeApp.component.SpeakingBird
import org.example.composeApp.util.Orientation

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    SpeakingBird(
        orientation = Orientation.Horizontal,
        enabled = true,
    ) {
        Column {
            Text("Hello, World!")
        }
    }
}