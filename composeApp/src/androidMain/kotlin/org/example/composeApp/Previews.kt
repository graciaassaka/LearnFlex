package org.example.composeApp

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.composeApp.component.ShimmerBox

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    ShimmerBox(
        isLoading = true,
        height = 20.dp,
        width = 40.dp,
        modifier = Modifier
    ) {
        Column {
            Text("Hello, World!")
        }
    }
}