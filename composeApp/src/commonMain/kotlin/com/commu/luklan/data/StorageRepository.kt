package com.commu.luklan.data

interface StorageRepository {
    suspend fun uploadImage(path: String, bytes: ByteArray): Result<String>
}

expect fun getStorageRepository(): StorageRepository
