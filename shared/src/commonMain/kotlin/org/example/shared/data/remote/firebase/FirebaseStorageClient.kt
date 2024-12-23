package org.example.shared.data.remote.firebase

import org.example.shared.domain.client.StorageClient

/**
 * Expects a platform-specific implementation of the FirebaseStorageClient.
 * This class should extend the StorageClient interface.
 */
expect class FirebaseStorageClient : StorageClient