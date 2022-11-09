package com.testarossa.template.library.android.widgets.shape_ripple

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class Image(private val bitmapResource: Int) : BaseShape() {
    private var bitmap: Bitmap? = null
    private var rect: Rect? = null
    override fun onSetup(context: Context, shapePaint: Paint) {
        rect = Rect()
        bitmap = BitmapFactory.decodeResource(context.resources, bitmapResource)
    }

    override fun onDraw(
        canvas: Canvas,
        x: Int,
        y: Int,
        radiusSize: Float,
        color: Int,
        rippleIndex: Int,
        shapePaint: Paint
    ) {
        if (bitmap == null) return
        val currentImageSize = radiusSize.toInt()

        // Get the current alpha channel of the color
        val currentAlpha = 0xFF and (color shr 24)
        shapePaint.alpha = currentAlpha
        rect?.let {
            it[x - currentImageSize, y - currentImageSize, x + radiusSize.toInt()] =
                y + radiusSize.toInt()
            canvas.drawBitmap(bitmap!!, null, it, shapePaint)
        }
    }
}
