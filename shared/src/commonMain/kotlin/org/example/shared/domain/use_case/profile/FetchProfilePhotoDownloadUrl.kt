package org.example.shared.domain.use_case.profile

import org.example.shared.domain.client.StorageClient

class FetchProfilePhotoDownloadUrl(
    private val storageClient: StorageClient
) {
    suspend operator fun invoke(path: String) = storageClient.getFileUrl(path)
}