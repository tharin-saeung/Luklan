package com.commu.luklan.data

import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await

class AndroidStorageRepository : StorageRepository {
    private val storage by lazy { FirebaseStorage.getInstance() }

    override suspend fun uploadImage(path: String, bytes: ByteArray): Result<String> {
        val defaultBucket = com.google.firebase.FirebaseApp.getInstance().options.storageBucket ?: ""
        android.util.Log.d("StorageRepo", "Attempting upload to default bucket: $defaultBucket, path: $path")
        
        return try {
            val ref = storage.reference.child(path)
            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            android.util.Log.e("StorageRepo", "Upload failed: ${e.message}", e)
            
            // Handle common 404 bucket mismatch or App Check failure (code -13010 or 403/404)
            val isNotFound = e.message?.contains("404") == true || (e as? com.google.firebase.storage.StorageException)?.errorCode == -13010
            
            if (isNotFound && defaultBucket.isNotEmpty()) {
                try {
                    val fallbackBucket = if (defaultBucket.endsWith(".firebasestorage.app")) {
                        defaultBucket.replace(".firebasestorage.app", ".appspot.com")
                    } else if (defaultBucket.endsWith(".appspot.com")) {
                        defaultBucket.replace(".appspot.com", ".firebasestorage.app")
                    } else {
                        null
                    }

                    if (fallbackBucket != null) {
                        android.util.Log.d("StorageRepo", "Retrying with fallback bucket: $fallbackBucket")
                        val fallbackStorage = FirebaseStorage.getInstance("gs://$fallbackBucket")
                        val ref = fallbackStorage.reference.child(path)
                        ref.putBytes(bytes).await()
                        val url = ref.downloadUrl.await().toString()
                        return Result.success(url)
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("StorageRepo", "Fallback upload also failed: ${e2.message}")
                    return Result.failure(e)
                }
            }
            Result.failure(e)
        }
    }
}

actual fun getStorageRepository(): StorageRepository = AndroidStorageRepository()
