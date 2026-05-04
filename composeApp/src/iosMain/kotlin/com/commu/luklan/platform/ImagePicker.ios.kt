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
actual suspend fun pickImageFromDevice(source: ImageSource): ByteArray? = withContext(Dispatchers.Main) {
    // NOTE: we avoid calling PHPhotoLibrary APIs here to keep Kotlin/Native interop simple.
    // The primary failure mode observed on first-run was that no presenter (window/rootViewController)
    // was available. We handle presenter selection robustly below and log window/presenter state.

    suspendCancellableCoroutine { continuation ->
        val picker = UIImagePickerController().apply {
            sourceType = if (source == ImageSource.CAMERA) {
                if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                } else {
                    UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                }
            } else {
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            }
            mediaTypes = listOf("public.image")
        }

        val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *> 
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                // Normalize, crop to square, and downscale to 512x512
                val processed = image?.let {
                    try {
                        val origW = it.size.useContents { width }
                        val origH = it.size.useContents { height }

                        // 1. Calculate square crop
                        val side = if (origW > origH) origH else origW
                        val x = (origW - side) / 2.0
                        val y = (origH - side) / 2.0

                        // 2. Draw cropped and scaled
                        val targetSize = CGSizeMake(512.0, 512.0)
                        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
                        
                        // Draw subset of original image
                        it.drawInRect(CGRectMake(0.0, 0.0, 512.0, 512.0)) 
                        // Note: drawInRect with different size handles scaling. 
                        // To crop AND scale, we use CGImage or specific drawing logic.
                        // Simple way: draw entire image into 512x512 but offset so center is square.
                        
                        // Better logic for square scaling:
                        val scale = 512.0 / side
                        val drawW = origW * scale
                        val drawH = origH * scale
                        val drawX = (512.0 - drawW) / 2.0
                        val drawY = (512.0 - drawH) / 2.0
                        
                        it.drawInRect(CGRectMake(drawX, drawY, drawW, drawH))
                        
                        val newImg = UIGraphicsGetImageFromCurrentImageContext()
                        UIGraphicsEndImageContext()
                        
                        newImg ?: it
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        it
                    }
                }

                // Try JPEG at 0.8 quality
                val data = processed?.let { UIImageJPEGRepresentation(it, 0.8) } ?: processed?.let { UIImagePNGRepresentation(it) }

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
