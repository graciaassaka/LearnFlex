package org.example.shared.data.remote.util

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * A handler for processing HTTP responses.
 *
 * @param T The type of the successful response.
 * @param response The HTTP response to handle.
 */
class HttpResponseHandler<T>(private val response: HttpResponse) {

    /**
     * Invokes the handler to process the HTTP response.
     *
     * @param handleSuccess A suspend function to handle a successful response.
     * @return The result of the handleSuccess function if the response is successful.
     * @throws ApiError.BadRequest If the response status is 400.
     * @throws ApiError.Unauthorized If the response status is 401.
     * @throws ApiError.Forbidden If the response status is 403.
     * @throws ApiError.NotFound If the response status is 404.
     * @throws ApiError.RateLimitExceeded If the response status is 429.
     * @throws ApiError.NetworkError If the response status is 503.
     * @throws ApiError.ServerError For any other response status.
     */
    suspend operator fun invoke(handleSuccess: suspend () -> T): T = response.run {
        val errorContent = if (!status.isSuccess()) body<ApiError.ErrorContainer>() else null
        when (status.value) {
            200  -> handleSuccess()

            400  -> throw ApiError.BadRequest(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            401  -> throw ApiError.Unauthorized(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            403  -> throw ApiError.Forbidden(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            404  -> throw ApiError.NotFound(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            429  -> throw ApiError.RateLimitExceeded(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            503  -> throw ApiError.NetworkError(
                requestPath = request.url.encodedPath,
                errorContainer = errorContent
            )

            else -> throw ApiError.ServerError(
                requestPath = request.url.encodedPath, status.value,
                errorContainer = errorContent
            )
        }
    }
}