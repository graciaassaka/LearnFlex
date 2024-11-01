package org.example.shared.data.util

/**
 * A sealed class representing different types of storage-related exceptions.
 */
sealed class StorageException : Exception() {

    /**
     * Exception thrown when an upload operation fails.
     *
     * @property message The detail message string.
     * @property cause The cause of the failure, or null if unknown.
     */
    data class UploadFailure(
        override val message: String,
        override val cause: Throwable? = null
    ) : StorageException()

    /**
     * Exception thrown when a download operation fails.
     *
     * @property message The detail message string.
     * @property cause The cause of the failure, or null if unknown.
     */
    data class DownloadFailure(
        override val message: String,
        override val cause: Throwable? = null
    ) : StorageException()

    /**
     * Exception thrown when a delete operation fails.
     *
     * @property message The detail message string.
     * @property cause The cause of the failure, or null if unknown.
     */
    data class DeleteFailure(
        override val message: String,
        override val cause: Throwable? = null
    ) : StorageException()
}