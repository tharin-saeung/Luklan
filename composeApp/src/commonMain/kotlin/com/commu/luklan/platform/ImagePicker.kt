package com.commu.luklan.platform

/**
 * Platform-specific image picker. Returns image bytes or null if not available.
 * Provide actual implementations in androidMain / iosMain. Stubs are provided to compile.
 */
expect suspend fun pickImageFromDevice(): ByteArray?
