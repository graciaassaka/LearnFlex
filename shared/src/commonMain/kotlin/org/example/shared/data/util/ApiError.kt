package org.example.shared.data.util

/**
 * Represents different types of API errors.
 *
 * @param message The error message.
 */
sealed class ApiError(message: String? = null): Exception(message) {

    /**
     * Represents an unauthorized error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param message The error message.
     */
    data class Unauthorized(
        val requestPath: String,
        override val message: String = "⚠️ Authentication required"
    ): ApiError(message)

    /**
     * Represents a forbidden error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param message The error message.
     */
    data class Forbidden(
        val requestPath: String,
        override val message: String = "⛔ Access denied"
    ): ApiError(message)

    /**
     * Represents a not found error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param message The error message.
     */
    data class NotFound(
        val requestPath: String,
        override val message: String = "❌ Resource not found"
    ): ApiError(message)

    /**
     * Represents a server error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param errorCode The error code returned by the server.
     * @param message The error message.
     */
    data class ServerError(
        val requestPath: String,
        val errorCode: Int,
        override val message: String = "❌ Server error"
    ): ApiError(message)

    /**
     * Represents a network error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param message The error message.
     */
    data class NetworkError(
        val requestPath: String,
        override val message: String = "❌ Network connection failed"
    ): ApiError(message)

    /**
     * Represents a rate limit exceeded error.
     *
     * @param requestPath The path of the request that caused the error.
     * @param message The error message.
     */
    data class RateLimitExceeded(
        val requestPath: String,
        override val message: String = "❌ Rate limit exceeded"
    ): ApiError(message)
}