package com.commu.luklan.data

import cocoapods.FirebaseFunctions.FIRFunctions
import cocoapods.FirebaseFunctions.FIRHTTPSCallableResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class VisionRepositoryIos : VisionRepository {

    // Ensure region matches your deployed function
    @OptIn(ExperimentalForeignApi::class)
    private val functions = FIRFunctions.functionsForRegion("asia-southeast1")

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun recognizeText(imageBytes: ByteArray): Result<String> {
        val maxAttempts = 3
        var lastEx: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                val base64encoded = encodeImageToBase64(imageBytes)

                // 1. Create request map (Equivalent to Swift Dictionary)
                val requestData = mapOf(
                    "image" to mapOf("content" to base64encoded),
                    "features" to listOf(mapOf("type" to "TEXT_DETECTION")),
                    "imageContext" to mapOf("languageHints" to listOf("th"))
                )

                // Wrap in batch requests array (Cloud Vision expects { requests: [ ... ] })
                val payload = mapOf("requests" to listOf(requestData))

                // 2. Invoke the callable function using coroutines
                val result = callFunction("annotateImage", payload)

                // 3. Extract text from dictionary
                // Support both old single-response shape and batch responses.
                val dataMap = result.data() as? Map<*, *>
                var extractedText: String = ""

                // Try batch-style response first: { responses: [ { fullTextAnnotation: { text: "..." } } ] }
                val responses = dataMap?.get("responses") as? List<*>
                if (!responses.isNullOrEmpty()) {
                    val first = responses[0] as? Map<*, *>
                    val ann = first?.get("fullTextAnnotation") as? Map<*, *>
                    extractedText = ann?.get("text") as? String ?: ""
                } else {
                    // Fallback to older single-object shape
                    val annotationList = dataMap?.get("fullTextAnnotation") as? Map<*, *>
                    extractedText = annotationList?.get("text") as? String ?: ""
                }

                return Result.success(extractedText)

            } catch (e: Exception) {
                e.printStackTrace()
                lastEx = e
                if (attempt < maxAttempts) {
                    val backoff = 300L * (1 shl (attempt - 1))
                    println("VisionRepositoryIos: attempt $attempt failed, retrying after ${'$'}backoff ms")
                    delay(backoff)
                    continue
                } else {
                    return Result.failure(e)
                }
            }

        }

        return Result.failure(lastEx ?: Exception("Unknown error"))
    }

    override suspend fun warmUp(): Result<Unit> {
        return try {
            // Lightweight client-side warm up: brief delay to allow SDK init.
            delay(500)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper function to convert iOS callback to Kotlin Suspend function
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun callFunction(name: String, data: Any): FIRHTTPSCallableResult =
        suspendCancellableCoroutine { continuation ->
            functions.HTTPSCallableWithName(name).callWithObject(data) { result, error ->
                if (error != null) {
                    // Include as much NSError info as possible for diagnostics: domain, code, localizedDescription and userInfo
                    try {
                        val domain = try { error.domain ?: "" } catch (t: Throwable) { "" }
                        val code = try { error.code.toString() } catch (t: Throwable) { "" }
                        val localized = try { error.localizedDescription ?: "" } catch (t: Throwable) { "" }
                        val userInfo = try { error.userInfo?.toString() } catch (t: Throwable) { null }

                        val parts = listOfNotNull(
                            if (domain.isNotEmpty()) "domain=$domain" else null,
                            if (code.isNotEmpty()) "code=$code" else null,
                            if (localized.isNotEmpty()) "localized=$localized" else null,
                            userInfo?.let { "userInfo=$it" }
                        )

                        val msg = if (parts.isNotEmpty()) parts.joinToString(" | ") else (error.localizedDescription ?: "INTERNAL")
                        println("VisionRepositoryIos.callFunction error: $msg")
                        continuation.resumeWithException(Exception(msg))
                    } catch (inner: Throwable) {
                        // Fallback to a minimal description
                        val fallback = try { error.localizedDescription ?: "INTERNAL" } catch (t: Throwable) { "INTERNAL" }
                        println("VisionRepositoryIos.callFunction error (fallback): $fallback")
                        continuation.resumeWithException(Exception(fallback))
                    }
                } else if (result != null) {
                    continuation.resume(result)
                } else {
                    continuation.resumeWithException(Exception("Unknown error occurred"))
                }
            }
        }
    }
actual fun getVisionRepository(): VisionRepository = VisionRepositoryIos()