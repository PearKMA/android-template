package com.testarossa.template.library.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.testarossa.template.library.android.data.model.ViewLocation
import com.testarossa.template.library.android.utils.toPixel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class DynamicImageView : AppCompatImageView {
    // region Const and Fields
    private var lastEvent: FloatArray? = null
    private var d = 0f
    private var parentWidth = 0
    private var parentHeight = 0
    private var viewParent: View? = null
    private var newRot = 0f
    private var margin = 0f
    private var isZoomAndRotate = false
    private var isOutSide = false
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f
    private var listener: IMovableListener? = null

    private val handler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { mode = DRAG }
    private var startPointerDown = 0L

    private var savedBitmap: Bitmap? = null
    private var ratio: Pair<Int, Int>? = null

    interface IMovableListener {
        fun onMoving(
            top: Boolean,
            left: Boolean,
            right: Boolean,
            bottom: Boolean,
            centerHorizontal: Boolean,
            centerVertical: Boolean
        )

        fun onSwipeLTR()
        fun onSwipeRTL()
        fun onRelease()

        fun onTap()
    }
    // endregion

    // region override/ listener method
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        margin = 0f.toPixel
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent?.requestDisallowInterceptTouchEvent(true)
        bringToFront()
        viewTransformation(this, event)
        return true
    }

    fun setMoveListener(parent: View, listener: IMovableListener) {
        viewParent = parent
        this.listener = listener
    }

    fun setRatio(ratio: Pair<Int, Int>, location: ViewLocation? = null, height: Int = 0) {
        this.ratio = ratio
        savedBitmap?.let { bitmap ->
            val resized = if (ratio.first == 0) {
                val bWidth = ratio.second * bitmap.width / bitmap.height
                Bitmap.createScaledBitmap(bitmap, bWidth, ratio.second, false)
            } else {
                val bHeight = ratio.first * bitmap.height / bitmap.width
                Bitmap.createScaledBitmap(bitmap, ratio.first, bHeight, false)
            }
            setImageBitmap(resized)
        }
        scaleX = location?.scaleX ?: 1f
        scaleY = location?.scaleY ?: 1f
        x = location?.x ?: margin
        y = location?.y ?: if (height > (savedBitmap?.height ?: 0) / 2) (height / 2f) else margin
    }

    fun initBitmap(bitmap: Bitmap, location: ViewLocation?) {
        savedBitmap = bitmap
        if (ratio != null) {
            val resized = if (ratio!!.first == 0) {
                val bWidth = ratio!!.second * bitmap.width / bitmap.height
                Bitmap.createScaledBitmap(bitmap, bWidth, ratio!!.second, false)
            } else {
                val bHeight = ratio!!.first * bitmap.height / bitmap.width
                Bitmap.createScaledBitmap(bitmap, ratio!!.first, bHeight, false)
            }
            setImageBitmap(resized)
        } else {
            setImageBitmap(bitmap)
        }
        setViewLocation(location)
    }

    fun setViewLocation(location: ViewLocation?) {
        scaleX = location?.scaleX ?: 1f
        scaleY = location?.scaleY ?: 1f
        x = location?.x ?: margin
        y = location?.y ?: ((viewParent?.height?.toFloat() ?: 100f.toPixel) / 2f)
    }

    fun onParentViewChange(height: Int? = null) {
        scaleX = 1f
        scaleY = 1f
        x = margin
        y = (height?.toFloat() ?: (viewParent?.height?.toFloat() ?: 100f.toPixel)) / 2f
    }

    fun restore() {
        try {
            viewParent?.post {
                val left = (width * scaleX - width) / 2f + margin
                val top = (height * scaleY - height) / 2f + margin
                val right = parentWidth - margin - width * scaleY + (width * scaleY - width) / 2f
                val bottom =
                    parentHeight - margin - height * scaleY + (height * scaleY - height) / 2f
                if (x < left || y < top || x > right || y > bottom) {
                    x = (width * scaleX - width) / 2f + margin
                    y = (viewParent?.height?.toFloat() ?: 100f.toPixel) / 2f
                }
            }
        } catch (_: Exception) {
        }
    }

    fun getViewLocation(): ViewLocation {
        return ViewLocation(scaleX, scaleY, x, y)
    }

    fun getBitmap() = savedBitmap

    fun onRelease() {
        try {
            savedBitmap?.recycle()
        } catch (_: Exception) {
        }
    }
    // endregion

    // region private method
    private fun viewTransformation(view: View, event: MotionEvent?) {
        if (event == null) return
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                startPointerDown = System.currentTimeMillis()
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY
                parentWidth = viewParent?.width ?: 0
                parentHeight = viewParent?.height ?: 0

                start.set(event.x, event.y)
                isOutSide = false
                handler.postDelayed(mRunnable, 700L)
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }

                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }

            MotionEvent.ACTION_UP -> {
                isZoomAndRotate = false
                handler.removeCallbacks(mRunnable)
                if (mode != DRAG && mode != ZOOM) {
                    val deltaX = event.x - start.x
                    if (abs(deltaX) > 50) {
                        if (event.x > start.x) {
                            listener?.onSwipeLTR()
                        } else {
                            listener?.onSwipeRTL()
                        }
                    } else if (System.currentTimeMillis() - startPointerDown < 350) {
                        listener?.onTap()
                    }
                }
                mode = NONE
//                if (mode == DRAG) {
//
//                }
                listener?.onRelease()
            }

            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_POINTER_UP -> {
//                mode = NONE
                lastEvent = null
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false

                        var newX = (event.rawX + xCoOrdinate)
                        var newY = (event.rawY + yCoOrdinate)

                        val dWidth = view.width * view.scaleX
                        val dHeight = view.height * view.scaleY
                        val left = newX - (dWidth - view.width) / 2f
                        val top = newY - (dHeight - view.height) / 2f
                        //if image will go outside left bound
                        if (left < margin) {
                            newX = (dWidth - view.width) / 2f + margin
                        }
                        //if image will go outside right bound
                        if (left + margin + dWidth > parentWidth) {
                            newX = parentWidth - margin - dWidth + (dWidth - view.width) / 2f
                        }
                        //if image will go oustside top bound
                        if (top < margin) {
                            newY = (dHeight - view.height) / 2f + margin
                        }
                        //if image will go outside bottom bound
                        if (top + margin + dHeight > parentHeight) {
                            newY = parentHeight - margin - dHeight + (dHeight - view.height) / 2f
                        }
                        view.animate().x(newX).y(newY).setDuration(0).start()
                    }
                    if (mode == ZOOM && event.pointerCount == 2) {
                        val newDist1 = spacing(event)
                        if (newDist1 > 10f) {
                            val scale = newDist1 / oldDist * view.scaleX
                            if (scale in 0.6..1.5) {
                                view.scaleX = scale
                                view.scaleY = scale
                            }
                        }
                        // prevent rotation
//                        if (lastEvent != null) {
//                            newRot = rotation(event)
//                            view.rotation = (view.rotation + (newRot - d))
//                        }
                    }
                    if (mode != NONE) {
                        val left = view.x - (view.width * view.scaleX - view.width) / 2f
                        val top = view.y - (view.height * view.scaleY - view.height) / 2f
                        val centerX = left + (view.width * view.scaleX) / 2f
                        val centerY = top + (view.height * view.scaleY) / 2f

                        val isLeft = left == margin
                        val isTop = top == margin
                        val isRight =
                            (left + view.width * view.scaleX + margin).toInt() == parentWidth
                        val isBottom =
                            (top + view.height * view.scaleY + margin).toInt() == parentHeight
                        val isCenterVertical = centerY.toInt() == parentHeight / 2
                        val isCenterHorizontal = centerX.toInt() == parentWidth / 2
                        listener?.onMoving(
                            isTop,
                            isLeft,
                            isRight,
                            isBottom,
                            isCenterHorizontal,
                            isCenterVertical
                        )
                    }
                }
            }
        }

    }

    private fun rotation(event: MotionEvent): Float {
        val deltaX = (event.getX(0) - event.getX(1))
        val deltaY = (event.getY(0) - event.getY(1))
        val radians = atan2(deltaY, deltaX)
        return Math.toDegrees(radians.toDouble()).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    // endregion

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }
}

