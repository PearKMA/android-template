package com.testarossa.template.library.android.widgets.shape_ripple

import android.graphics.Color

class ShapeRippleEntry(
    /**
     * The shape renderer of the ripple
     */
    var baseShape: BaseShape
) {

    /**
     * Flag for when the ripple is ready to be rendered
     * to the view
     */
    var isRender = false

    /**
     * The current radius size of the ripple
     */
    var radiusSize = 0f

    /**
     * The current multiplier value of the ripple
     */
    var multiplierValue = 0f

    /**
     * The current index of the ripple in the list
     * from [ShapeRipple.shapeRippleEntries]
     */
    var rippleIndex = 0

    /**
     * The X position of the ripple, defaulted to the middle of the view
     */
    var x = 0

    /**
     * The Y position of the ripple, defaulted to the middle of the view
     */
    var y = 0

    /**
     * The original color value which is only changed when view is created or
     * the ripple list is re configured
     */
    private var originalColorValue = 0

    /**
     * The changeable color value which is used when color transition,
     * on measure to the view, when render process happens
     */
    var changingColorValue = 0

    fun getOriginalColorValue(): Int {
        return originalColorValue
    }

    fun setOriginalColorValue(originalColorValue: Int) {
        this.originalColorValue = originalColorValue
        changingColorValue = originalColorValue
    }

    /**
     * Reset all data of this shape ripple entries
     */
    fun reset() {
        isRender = false
        multiplierValue = -1f
        radiusSize = 0f
        originalColorValue = Color.TRANSPARENT
        changingColorValue = Color.TRANSPARENT
    }
}
