package com.commu.luklan.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import platform.Foundation.NSData
import platform.Foundation.*
import platform.UIKit.*
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.darwin.*

// Keep strong references to active delegates so Kotlin/Native doesn't GC them while
// UIKit still expects them to exist. We'll remove them once the picker finishes.
private val activeImagePickerDelegates = mutableListOf<NSObject>()

@OptIn(ExperimentalForeignApi::class)
private fun nsDataToByteArray(data: NSData): ByteArray {
    val len = data.length.toInt()
    val result = ByteArray(len)
    if (len > 0) {
        result.usePinned { pinned ->
            data.getBytes(pinned.addressOf(0), len.toULong())
        }
    }
    return result
}

// iOS implementation using UIImagePickerController
@OptIn(ExperimentalForeignApi::class)
actual suspend fun pickImageFromDevice(): ByteArray? = withContext(Dispatchers.Main) {
    // NOTE: we avoid calling PHPhotoLibrary APIs here to keep Kotlin/Native interop simple.
    // The primary failure mode observed on first-run was that no presenter (window/rootViewController)
    // was available. We handle presenter selection robustly below and log window/presenter state.

    suspendCancellableCoroutine { continuation ->
        val picker = UIImagePickerController().apply {
            sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            mediaTypes = listOf("public.image")
        }

        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *> 
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                // Normalize image by drawing into a standard bitmap context to avoid
                // color space / headroom issues for Display P3 images. Then downscale
                // large images to reduce payload size (helps avoid server-side errors).
                val normalized = image?.let {
                    try {
                        val origW = it.size.useContents { width }
                        val origH = it.size.useContents { height }

                        // First, rasterize into sRGB-like bitmap using current scale.
                        UIGraphicsBeginImageContextWithOptions(it.size, false, it.scale)
                        it.drawInRect(CGRectMake(0.0, 0.0, origW, origH))
                        var newImg = UIGraphicsGetImageFromCurrentImageContext()
                        UIGraphicsEndImageContext()

                        if (newImg == null) newImg = it

                        // Downscale if either dimension exceeds maxDim
                        val maxDim = 1024.0
                        val maxOrig = maxOf(origW, origH)
                        if (maxOrig > maxDim) {
                            val scale = maxDim / maxOrig
                            val targetW = (origW * scale)
                            val targetH = (origH * scale)
                            UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetW, targetH), false, 1.0)
                            newImg.drawInRect(CGRectMake(0.0, 0.0, targetW, targetH))
                            val scaledImg = UIGraphicsGetImageFromCurrentImageContext()
                            UIGraphicsEndImageContext()
                            if (scaledImg != null) {
                                println("ImagePicker: downscaled image from ${origW}x${origH} to ${targetW}x${targetH}")
                                newImg = scaledImg
                            }
                        }

                        newImg
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        it
                    }
                }

                // Try JPEG first, fall back to PNG if JPEG encoding fails for any reason.
                val data = normalized?.let { UIImageJPEGRepresentation(it, 0.75) } ?: normalized?.let { UIImagePNGRepresentation(it) }

                val bytes = data?.let { nsDataToByteArray(it) }
                try {
                    continuation.resume(bytes)
                } catch (e: Throwable) {
                    // ignore already-resumed
                }
                picker.dismissViewControllerAnimated(true, completion = null)
                try {
                    activeImagePickerDelegates.remove(this)
                } catch (_: Throwable) {}
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                try {
                    continuation.resume(null)
                } catch (e: Throwable) {
                    // ignore
                }
                picker.dismissViewControllerAnimated(true, completion = null)
                try {
                    activeImagePickerDelegates.remove(this)
                } catch (_: Throwable) {}
            }
        }

        // Keep a strong reference until picker finishes/cancels
        activeImagePickerDelegates.add(delegate)
        picker.delegate = delegate

        // Present from top-most view controller for greater reliability
        fun topViewController(vc: UIViewController?): UIViewController? {
            var current = vc
            while (current?.presentedViewController != null) {
                current = current.presentedViewController
            }
            return current
        }

        // Find a presenter view controller in a scene/window-aware manner. Avoid relying on `keyWindow` which
        // may be nil on first launch (iOS 13+ scenes). Fall back to other windows if needed.
        val app = UIApplication.sharedApplication
    val windows = app.windows
    val key = app.keyWindow
    val windowsCount = windows?.count()?.toInt() ?: 0
    println("ImagePicker: windows.count=${windowsCount} keyWindow=${key != null}")

        var rootVC: UIViewController? = key?.rootViewController
        if (rootVC == null && windows != null && windows.isNotEmpty()) {
            for (i in 0 until windows.size) {
                val w = windows[i] as? UIWindow
                if (w != null && w.rootViewController != null) {
                    rootVC = w.rootViewController
                    break
                }
            }
        }

        if (rootVC == null) {
            try {
                val appDelWindow = app.delegate?.window as? UIWindow
                if (appDelWindow != null && appDelWindow.rootViewController != null) rootVC = appDelWindow.rootViewController
            } catch (e: Throwable) {
                // ignore
            }
        }

        val presenter = topViewController(rootVC)
        println("ImagePicker: presenter found=${presenter != null}")

        if (presenter != null) {
            presenter.presentViewController(picker, true, null)
        } else {
            // No presenter available; surface a null so caller can show an appropriate error instead of
            // silently treating it as a picked-but-empty image.
            continuation.resume(null)
        }

        continuation.invokeOnCancellation {
            try {
                picker.dismissViewControllerAnimated(true, completion = null)
            } catch (e: Throwable) {
                // ignore
                e.printStackTrace()
            }
        }
    }
}

private fun List<*>.objectAtIndex(toULong: Any) {}
