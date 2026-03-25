package com.commu.luklan.data

import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay

class VisionRepositoryAndroid : VisionRepository {

    // NOTE: Make sure to use the region you deployed to!
    private val functions = FirebaseFunctions.getInstance("asia-southeast1")

    override suspend fun recognizeText(imageBytes: ByteArray): Result<String> {
        // Retry/backoff to tolerate first-call SDK/network warm-up issues.
        val maxAttempts = 3
        var lastEx: Exception? = null
        for (attempt in 1..maxAttempts) {
            try {
                val base64encoded = encodeImageToBase64(imageBytes)

                // 1. Create request map matching the iOS callable payload (Map<String, Any>)
                val requestMap: Map<String, Any> = mapOf(
                    "image" to mapOf("content" to base64encoded),
                    "features" to listOf(mapOf("type" to "TEXT_DETECTION")),
                    "imageContext" to mapOf("languageHints" to listOf("th"))
                )

                // 2. Wrap request in a batch-style `requests` array (Cloud Vision expects this)
                val payload: Map<String, Any> = mapOf("requests" to listOf(requestMap))

                // Invoke the callable function with a Map (not a JSON string)
                val result = functions
                    .getHttpsCallable("annotateImage")
                    .call(payload)
                    .await()

                // 3. Extract text from the result. Support batch responses or the older single shape.
                val resultData = result.data as? Map<*, *>
                var extractedText: String = ""

                val responses = resultData?.get("responses") as? List<*>
                if (!responses.isNullOrEmpty()) {
                    val first = responses[0] as? Map<*, *>
                    val ann = first?.get("fullTextAnnotation") as? Map<*, *>
                    extractedText = ann?.get("text") as? String ?: ""
                } else {
                    val annotation = resultData?.get("fullTextAnnotation") as? Map<*, *>
                    extractedText = annotation?.get("text") as? String ?: ""
                }

                return Result.success(extractedText)
            } catch (e: Exception) {
                e.printStackTrace()
                lastEx = e
                if (attempt < maxAttempts) {
                    val backoff = 300L * (1 shl (attempt - 1))
                    println("VisionRepositoryAndroid: attempt $attempt failed, retrying after ${'$'}backoff ms")
                    delay(backoff)
                    continue
                } else {
                    return try {
                        val ffe = e as? com.google.firebase.functions.FirebaseFunctionsException
                        val details = ffe?.details?.toString()
                        val msg = listOfNotNull(e.message, details).joinToString(" - ")
                        Result.failure(Exception(msg))
                    } catch (inner: Exception) {
                        Result.failure(e)
                    }
                }
            }
        }
        return Result.failure(lastEx ?: Exception("Unknown error"))
    }

    override suspend fun warmUp(): Result<Unit> {
        // Lightweight client-side warm up: brief delay to allow SDK init.
        return try {
            delay(500)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

actual fun getVisionRepository(): VisionRepository = VisionRepositoryAndroid()