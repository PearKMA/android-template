package com.testarossa.template.library.android.widgets.shape_ripple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

class Circle : BaseShape() {
    override fun onSetup(context: Context, shapePaint: Paint) {}
    override fun onDraw(
        canvas: Canvas,
        x: Int,
        y: Int,
        radiusSize: Float,
        color: Int,
        rippleIndex: Int,
        shapePaint: Paint
    ) {

        shapePaint.color = color
        canvas.drawCircle(x.toFloat(), y.toFloat(), radiusSize, shapePaint)
    }
}
