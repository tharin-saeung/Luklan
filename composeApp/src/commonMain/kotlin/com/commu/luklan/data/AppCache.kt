package com.commu.luklan.data

/**
 * In-memory cache to prevent UI flickers during Compose recompositions and screen navigation.
 * Firestore already provides disk-based offline persistence, but fetching from it is still asynchronous.
 * This cache ensures the UI has data instantly available on first render.
 */
object AppCache {
    val medicinesCache = mutableMapOf<String, List<Medicine>>()
    val groupsCache = mutableMapOf<String, List<CareGroup>>()
    val userProfileCache = mutableMapOf<String, User>()
    
    fun clear() {
        medicinesCache.clear()
        groupsCache.clear()
        userProfileCache.clear()
    }
}
