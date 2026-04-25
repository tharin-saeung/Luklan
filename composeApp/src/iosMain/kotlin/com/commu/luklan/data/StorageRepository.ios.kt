package com.commu.luklan.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class IosStorageRepository : StorageRepository {
    override suspend fun uploadImage(path: String, bytes: ByteArray): Result<String> = suspendCoroutine { continuation ->
        val nsData = bytes.toNSData()
        platform.FirestoreBridge.FirestoreBridge.uploadImageWithData(
            data = nsData,
            path = path
        ) { url: String?, error: String? ->
            if (error != null) {
                continuation.resume(Result.failure(Exception(error)))
            } else if (url != null) {
                continuation.resume(Result.success(url))
            } else {
                continuation.resume(Result.failure(Exception("Unknown error during upload")))
            }
        }
    }

    private fun ByteArray.toNSData(): NSData = usePinned {
        NSData.create(bytes = it.addressOf(0), length = size.toULong())
    }
}

actual fun getStorageRepository(): StorageRepository = IosStorageRepository()
