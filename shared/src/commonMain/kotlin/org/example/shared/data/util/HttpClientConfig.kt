package org.example.shared.data.util

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Object for configuring the HTTP client.
 */
object HttpClientConfig {
    fun create(): HttpClient = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 60000L
            connectTimeoutMillis = 60000L
            socketTimeoutMillis = 60000L
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }

        engine {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(60_000, TimeUnit.MILLISECONDS)
                readTimeout(60_000, TimeUnit.MILLISECONDS)
                writeTimeout(60_000, TimeUnit.MILLISECONDS)
            }
        }
    }
}