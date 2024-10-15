package org.example.composeApp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import learnflex.composeapp.generated.resources.Res
import learnflex.composeapp.generated.resources.compose_multiplatform
import org.example.composeApp.navigation.Navigator
import org.example.composeApp.theme.LearnFlexTheme
import org.example.shared.Greeting
import org.example.shared.domain.service.AuthService
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    LearnFlexTheme {

    }
}