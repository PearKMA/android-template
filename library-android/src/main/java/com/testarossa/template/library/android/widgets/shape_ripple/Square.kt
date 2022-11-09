package com.testarossa.template.library.android.widgets.shape_ripple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect

class Square : BaseShape() {
    private var rect: Rect? = null

    override fun onSetup(context: Context, shapePaint: Paint) {
        rect = Rect()
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
        rect?.let {
            it.left = (x - radiusSize).toInt()
            it.right = (x + radiusSize).toInt()
            it.top = (y - radiusSize).toInt()
            it.bottom = (y + radiusSize).toInt()
            shapePaint.color = color
            canvas.drawRect(it, shapePaint)
        }
    }
}
