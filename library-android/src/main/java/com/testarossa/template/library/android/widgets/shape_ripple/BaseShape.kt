package com.testarossa.template.library.android.widgets.shape_ripple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

abstract class BaseShape {
    /**
     * @return The width of the layout in pixel
     */
    /**
     * Change the width of the layout
     *
     * @param width The width of the layout in pixel
     */
    /**
     * The width of the layout in pixel
     */
    var width = 0
    /**
     * @return The height of the layout in pixel
     */
    /**
     * Change the height of the layout
     *
     * @param height The height of the layout in pixel
     */
    /**
     * The height of the layout in pixel
     */
    var height = 0

    /**
     * Setup method for the [BaseShape] before ripple rendering happens.
     *
     * NOTE: This is only called once every time the [BaseShape] is attached to the
     * [ShapeRipple]
     *
     * @param context The [ShapeRipple] context
     * @param shapePaint The Paint that the ripple uses to render in the canvas.
     */
    abstract fun onSetup(context: Context, shapePaint: Paint)

    /**
     * This will draw the actual ripple to the canvas.
     *
     * @param canvas The canvas where the ripple is drawn
     * @param x The x axis if the ripple, x means the middle x-axis.
     * @param y The y axis if the ripple, y means the middle y-axis.
     * @param radiusSize The current radius size if the ripple, this changes over time.
     * @param color The current color of the ripple, this changes over time.
     * @param rippleIndex The index of the ripple, 0 index is the middle and n-1 is the last outer ripple
     * @param shapePaint The paint of the ripple.
     */
    abstract fun onDraw(
        canvas: Canvas,
        x: Int,
        y: Int,
        radiusSize: Float,
        color: Int,
        rippleIndex: Int,
        shapePaint: Paint
    )
}

