package com.testarossa.template.library.android.widgets

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.viewpager2.widget.ViewPager2
import com.testarossa.template.library.android.R
import com.testarossa.template.library.android.widgets.indicator.Dot
import kotlin.math.min


interface IndicatorListener {
    fun setViewPager(viewPager: ViewPager2)
    fun setAnimateDuration(duration: Long)
    fun setRadiusSelected(radius: Int)
    fun setRadiusUnselected(radius: Int)
    fun setDistanceDot(distance: Int)
}

const val DEFAULT_ANIMATE_DURATION: Long = 200
const val DEFAULT_RADIUS_SELECTED = 20
const val DEFAULT_RADIUS_UNSELECTED = 15
const val DEFAULT_DISTANCE = 40


class IndicatorView : View, IndicatorListener {
    private var viewPager: ViewPager2? = null
    private val dots = arrayListOf<Dot>()
    private var animateDuration = DEFAULT_ANIMATE_DURATION
    private var radiusSelected = DEFAULT_RADIUS_SELECTED
    private var radiusUnSelected = DEFAULT_RADIUS_UNSELECTED
    private var distance = DEFAULT_DISTANCE

    @ColorInt
    private var colorSelected = 0

    @ColorInt
    private var colorUnSelected = 0
    private var currentPosition = 0
    private var beforePosition = 0
    private lateinit var animatorZoomIn: ValueAnimator
    private lateinit var animatorZoomOut: ValueAnimator

    constructor(context: Context?) : super(context) {
        initView(context, null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        if (null != context && null != attrs) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorView)
            this.radiusSelected = typedArray.getDimensionPixelSize(
                R.styleable.IndicatorView_radius_selected,
                DEFAULT_RADIUS_SELECTED
            )
            this.radiusUnSelected = typedArray.getDimensionPixelSize(
                R.styleable.IndicatorView_radius_unselected,
                DEFAULT_RADIUS_UNSELECTED
            )
            this.distance = typedArray.getInt(R.styleable.IndicatorView_distance, DEFAULT_DISTANCE)
            this.colorSelected =
                typedArray.getColor(R.styleable.IndicatorView_color_selected, Color.WHITE)
            this.colorUnSelected =
                typedArray.getColor(
                    R.styleable.IndicatorView_color_unselected,
                    Color.argb(80, 255, 255, 255)
                )
            typedArray.recycle()
        }

        for (i in 0 until 3) {
            dots.add(Dot())
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val yCenter = height / 2f
        val d = distance + 2 * radiusUnSelected
        val firstXCenter = (width / 2) - ((dots.size - 1) * d / 2f)
        for (i in 0 until dots.size) {
            dots[i].setCenter(if (i == 0) firstXCenter else firstXCenter + d * i, yCenter)
            dots[i].setCurrentRadius(if (i == currentPosition) radiusSelected else radiusUnSelected)
            dots[i].setColor(if (i == currentPosition) colorSelected else colorUnSelected)
            dots[i].setAlpha(if (i == currentPosition) 255 else radiusUnSelected * 255 / radiusSelected)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredHeight = 2 * radiusSelected

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val mWidth = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> widthSize
            else -> 0
        }
        val mHeight = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
        viewPager.registerOnPageChangeCallback(viewPager2Callback)
        viewPager.setCurrentItem(0, false)
    }

    private val viewPager2Callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            beforePosition = currentPosition
            currentPosition = position

            if (beforePosition == currentPosition) {
                beforePosition = currentPosition + 1
            }
            val animatorSet = AnimatorSet()
            animatorSet.duration = animateDuration

            animatorZoomIn = ValueAnimator.ofInt(radiusUnSelected, radiusSelected)
            animatorZoomIn.addUpdateListener(object : AnimatorUpdateListener {
                var positionPerform = currentPosition
                override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
                    val newRadius = valueAnimator.animatedValue as Int
                    changeNewRadius(positionPerform, newRadius)
                }
            })

            animatorZoomOut = ValueAnimator.ofInt(radiusSelected, radiusUnSelected)
            animatorZoomOut.addUpdateListener(object : AnimatorUpdateListener {
                var positionPerform = beforePosition
                override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
                    val newRadius = valueAnimator.animatedValue as Int
                    changeNewRadius(positionPerform, newRadius)
                }
            })

            animatorSet.play(animatorZoomIn).with(animatorZoomOut)
            animatorSet.start()
        }
    }

    private fun changeNewRadius(positionPerform: Int, newRadius: Int) {
        if (dots[positionPerform].getCurrentRadius() != newRadius) {
            dots[positionPerform].setCurrentRadius(newRadius)
            dots[positionPerform].setAlpha(newRadius * 255 / radiusSelected)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (null != canvas) {
            for (i in 0 until dots.size) {
                dots[i].draw(canvas, currentPosition == i)
            }
        }
    }

    override fun setAnimateDuration(duration: Long) {
        this.animateDuration = duration
    }

    override fun setRadiusSelected(radius: Int) {
        this.radiusSelected = radius
    }

    override fun setRadiusUnselected(radius: Int) {
        this.radiusUnSelected = radius
    }

    override fun setDistanceDot(distance: Int) {
        this.distance = distance
    }

}
