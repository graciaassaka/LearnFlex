package org.example.shared.data.remote.util

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
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

        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, httpResponse ->
                httpResponse.status.value in setOf(
                    HttpStatusCode.RequestTimeout.value,
                    HttpStatusCode.InternalServerError.value
                )
            }
            delayMillis { retry ->
                retry * 3000L
            }
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