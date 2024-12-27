package org.example.shared.data.remote.util

import kotlinx.serialization.Serializable

/**
 * Represents different types of errors that may occur when interacting with an API.
 *
 * @param message An optional message describing the error.
 */
sealed class ApiError(message: String? = null) : Exception(message) {
    /**
     * A container class that holds details of an error.
     *
     * @property error The details of the error, encapsulated in an [ErrorDetails] object.
     */
    @Serializable
    data class ErrorContainer(
        val error: ErrorDetails
    )

    /**
     * Represents the details of an error.
     *
     * @property message The error message providing information about what went wrong.
     * @property type The type of error, categorizing the nature of the error.
     * @property code An optional error code that provides a specific identifier for the error.
     */
    @Serializable
    data class ErrorDetails(
        val message: String = "",
        val type: String = "",
        val code: String? = null
    )

    /**
     * The path of the API request that resulted in an error.
     */
    abstract val requestPath: String
    /**
     * Optional container for additional error details.
     *
     * Instances of subclasses may use this property to include structured error information,
     * aiding in debugging and error handling.
     *
     * It can hold an instance of `ErrorContainer` which can encapsulate specific error details
     * relevant to the particular API error.
     */
    open val errorContainer: ErrorContainer? = null

    /**
     * A class representing a bad request error in an API response.
     *
     * @property requestPath The path of the request that resulted in the error.
     * @property message The error message, defaulted to "❌ Bad request".
     * @property errorContainer An optional container holding additional error details.
     */
    data class BadRequest(
        override val requestPath: String,
        override val message: String = "❌ Bad request",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * Represents an error indicating that authentication is required to access the requested resource.
     *
     * @property requestPath The path of the request that triggered the error.
     * @property message A message describing the error, defaulting to '⚠️ Authentication required'.
     * @property errorContainer An optional container for additional error details, which can be null.
     */
    data class Unauthorized(
        override val requestPath: String,
        override val message: String = "⚠️ Authentication required",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * Represents an error indicating that access to the requested resource is denied.
     *
     * @property requestPath The path of the request that resulted in the error.
     * @property message A message describing the error. Defaults to "⛔ Access denied".
     * @property errorContainer An optional container for additional error details.
     */
    data class Forbidden(
        override val requestPath: String,
        override val message: String = "⛔ Access denied",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * Represents a "Not Found" (404) error response in an API.
     *
     * This class extends the [ApiError] class and is used to indicate that a requested resource
     * could not be found. It includes details such as the request path and an optional error container.
     *
     * @property requestPath The path of the request that resulted in the "Not Found" error.
     * @property message The error message associated with this "Not Found" error. Defaults to "❌ Resource not found".
     * @property errorContainer An optional container with additional error details.
     */
    data class NotFound(
        override val requestPath: String,
        override val message: String = "❌ Resource not found",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * Represents a server error response from an API.
     *
     * @property requestPath The request path that caused the error.
     * @property errorCode The HTTP status code received from the server.
     * @property message The error message, with a default value indicating a server error.
     * @property errorContainer Optional container for detailed error information.
     */
    data class ServerError(
        override val requestPath: String,
        val errorCode: Int,
        override val message: String = "❌ Server error",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * A data class representing a network-related error encountered when making an API request.
     *
     * @property requestPath The path of the request that caused the error.
     * @property message The error message, with a default value indicating a network connection failure.
     * @property errorContainer Optional container with additional error details.
     */
    data class NetworkError(
        override val requestPath: String,
        override val message: String = "❌ Network connection failed",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)

    /**
     * Represents an error that occurs when the rate limit for an API request is exceeded.
     *
     * @property requestPath The path of the request that triggered the rate limit exceed error.
     * @property message The error message associated with the rate limit exceeds.
     * Defaults to "❌ Rate limit exceeded".
     * @property errorContainer An optional container for additional error details or metadata.
     */
    data class RateLimitExceeded(
        override val requestPath: String,
        override val message: String = "❌ Rate limit exceeded",
        override val errorContainer: ErrorContainer? = null
    ) : ApiError(message)
}

