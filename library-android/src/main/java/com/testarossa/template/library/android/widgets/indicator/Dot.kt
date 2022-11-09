package com.testarossa.template.library.android.widgets.indicator

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF

class Dot {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val center = PointF()
    private var currentRadius = 0
    private val rect = RectF()

    fun setColor(color: Int) {
        mPaint.color = color
    }

    fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    fun setCenter(x: Float, y: Float) {
        center.set(x, y)
    }

    fun getCurrentRadius() = currentRadius

    fun setCurrentRadius(radius: Int) {
        this.currentRadius = radius - 1
    }

    fun draw(canvas: Canvas, isSelected: Boolean) {
        if (isSelected) {
            mPaint.strokeWidth = 3f
            mPaint.style = Paint.Style.FILL_AND_STROKE
            rect.left = center.x - currentRadius * 1.6f
            rect.right = center.x + currentRadius * 1.6f
            rect.top = center.y - currentRadius / 1.5f
            rect.bottom = center.y + currentRadius / 1.5f
            canvas.drawRoundRect(rect, currentRadius.toFloat(), currentRadius.toFloat(), mPaint)
        } else {
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(center.x, center.y, currentRadius.toFloat(), mPaint)
        }
    }
}
