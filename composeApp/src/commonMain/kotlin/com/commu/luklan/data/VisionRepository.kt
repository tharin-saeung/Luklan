package com.commu.luklan.data

// Using Kotlin's built-in Base64 encoder (available in Kotlin 1.8.20+)
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface VisionRepository {
    suspend fun recognizeText(imageBytes: ByteArray): Result<String>
    // Optional warm-up hook to allow the client to pre-initialize SDKs or perform
    // a short delay before the first network call. Return Result.success(Unit)
    // when ready. Implementations should keep this lightweight.
    suspend fun warmUp(): Result<Unit>
}

@OptIn(ExperimentalEncodingApi::class)
fun encodeImageToBase64(imageBytes: ByteArray): String {
    return Base64.encode(imageBytes)
}

expect fun getVisionRepository(): VisionRepository