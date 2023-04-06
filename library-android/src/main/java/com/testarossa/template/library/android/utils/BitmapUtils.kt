@file:Suppress("DEPRECATION")

package com.testarossa.template.library.android.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.PixelCopy
import android.view.View
import androidx.core.view.drawToBitmap
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import java.io.File
import java.io.FileOutputStream

fun Bitmap?.saveFile(
    pictureFile: File,
    ext: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    quality: Int = 90,
    callback: (Boolean) -> Unit
) {
    try {
        val fos = FileOutputStream(pictureFile)
        this?.compress(ext, quality, fos)
        fos.close()
        callback(true)
    } catch (e: Exception) {
        e.printStackTrace()
        callback(false)
    }
}

fun View.toBitmap(action: (Bitmap) -> Unit, reAction: () -> Unit) {
    if (this.width > 0 && this.height > 0) {
        val bitmap = this.drawToBitmap()
        action(bitmap)
    } else {
        this.post {
            if (this.width > 0 && this.height > 0) {
                reAction()
            }
        }
    }
}

@Suppress("DEPRECATION")
@Deprecated("Deprecated. Use https://developer.android.com/guide/topics/renderscript/migrate")
fun Bitmap.blurBitmap(context: Context, radius: Float): Bitmap {
    lateinit var rsContext: RenderScript
    try {
        val output = Bitmap.createBitmap(this.width, this.height, this.config)
        //
        rsContext = RenderScript.create(context, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(rsContext, this)
        val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
        theIntrinsic.apply {
            setRadius(radius)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        rsContext.finish()
    }
}

fun getScreenShotFromPixelCopy(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
    activity.window?.let { window ->
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)
        try {
            if (isBuildLargerThan(Build.VERSION_CODES.O)) {
                PixelCopy.request(
                    window,
                    Rect(
                        locationOfViewInWindow[0],
                        locationOfViewInWindow[1],
                        locationOfViewInWindow[0] + view.width,
                        locationOfViewInWindow[1] + view.height
                    ), bitmap, { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        }
                        // possible to handle other result codes ...
                    },
                    Handler(Looper.getMainLooper())
                )
            } else {
                // Create bitmap and draw via canvas
                val canvas = Canvas(bitmap)
                view.draw(canvas)
                callback(bitmap)
            }
        } catch (e: IllegalArgumentException) {
            // PixelCopy may throw IllegalArgumentException, make sure to handle it
            e.printStackTrace()
        }
    }
}
