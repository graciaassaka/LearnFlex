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
import org.example.composeApp.ui.theme.LearnFlexTheme
import org.example.shared.Greeting
import org.example.shared.domain.service.AuthService
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    val authService: AuthService = koinInject()
    val coroutineScope = rememberCoroutineScope()

    LearnFlexTheme {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var showContent by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = authService.signIn(email, password)
                        result.fold(
                            onSuccess = {
                                showContent = true
                                errorMessage = null
                            },
                            onFailure = { error ->
                                errorMessage = error.message
                            }
                        )
                    }
                }
            ) {
                Text("Sign In")
            }

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Welcome! Compose: $greeting")
                }
            }
        }
    }
}