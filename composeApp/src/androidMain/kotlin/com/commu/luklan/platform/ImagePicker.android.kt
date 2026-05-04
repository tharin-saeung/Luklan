package com.commu.luklan.platform

import android.content.Intent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.commu.luklan.LuklanApplication

internal object ImagePickerHolder {
    var deferred: CompletableDeferred<ByteArray?>? = null
}

// Launch a small Activity to pick an image and return bytes via a CompletableDeferred.
actual suspend fun pickImageFromDevice(source: ImageSource): ByteArray? = withContext(Dispatchers.Main) {
    val deferred = CompletableDeferred<ByteArray?>()
    ImagePickerHolder.deferred = deferred

    val ctx = LuklanApplication.getAppContext()
    val intent = Intent(ctx, ImagePickerActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        putExtra("source", source.name)
    }
    ctx.startActivity(intent)

    try {
        deferred.await()
    } finally {
        ImagePickerHolder.deferred = null
    }
}
