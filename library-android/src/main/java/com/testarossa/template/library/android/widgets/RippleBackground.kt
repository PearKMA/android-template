package com.testarossa.template.library.android.widgets

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import androidx.core.animation.doOnStart
import com.testarossa.template.library.android.R
import com.testarossa.template.library.android.utils.extension.show
import com.testarossa.template.library.android.utils.toPixel


class RippleBackground : RelativeLayout {


    // region Const and Fields
    private var rippleColor = Color.parseColor("#3E309F")
    private var rippleStrokeWidth = 2f.toPixel
    private var rippleRadius = 64f.toPixel
    private var rippleDurationTime = DEFAULT_DURATION_TIME
    private var rippleAmount = DEFAULT_RIPPLE_COUNT
    private var rippleScale = DEFAULT_SCALE
    private var rippleType = DEFAULT_FILL_TYPE
    private var rippleDelay = 1000
    private val paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val animatorSet by lazy { AnimatorSet() }
    private val animatorList = ArrayList<Animator>()
    private lateinit var rippleParams: LayoutParams
    private val rippleViewList = ArrayList<RippleView>()
    private var animationRunning = false
    // endregion

    // region override/ listener method
    constructor(context: Context?) : super(context)
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
    // endregion

    // region private method
    private fun initView(context: Context?, attrs: AttributeSet?) {
        if (isInEditMode) return
        if (context == null) return
        if (attrs == null) throw IllegalArgumentException("Attributes should be provided to this view")

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground)
        rippleColor = typedArray.getColor(
            R.styleable.RippleBackground_rb_color, Color.parseColor("#3E309F")
        )
        rippleStrokeWidth = typedArray.getDimension(
            R.styleable.RippleBackground_rb_strokeWidth, resources.getDimension(
                com.intuit.sdp.R.dimen._2sdp
            )
        )
        rippleRadius = typedArray.getDimension(
            R.styleable.RippleBackground_rb_radius, resources.getDimension(
                com.intuit.sdp.R.dimen._49sdp
            )
        )
        rippleDurationTime =
            typedArray.getInt(R.styleable.RippleBackground_rb_duration, DEFAULT_DURATION_TIME)
        rippleAmount =
            typedArray.getInt(R.styleable.RippleBackground_rb_rippleAmount, DEFAULT_RIPPLE_COUNT)
        rippleScale = typedArray.getFloat(R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE)
        rippleType = typedArray.getInt(R.styleable.RippleBackground_rb_type, DEFAULT_FILL_TYPE)
        typedArray.recycle()

        rippleDelay = rippleDurationTime / rippleAmount
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0f
            paint.style = Paint.Style.FILL
        } else
            paint.style = Paint.Style.STROKE
        paint.color = rippleColor

        rippleParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        rippleParams.addRule(CENTER_IN_PARENT, TRUE)

        animatorSet.interpolator = AccelerateDecelerateInterpolator()
    }
    // endregion

    fun onCreateAnimation() {
        for (i in 0 until rippleAmount) {
            val rippleView = RippleView(context)
            addView(rippleView, rippleParams)
            rippleViewList.add(rippleView)
            val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 0.75f, rippleScale)
            scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleXAnimator.repeatMode = ObjectAnimator.RESTART
            scaleXAnimator.startDelay = (i * rippleDelay).toLong()
            scaleXAnimator.duration = rippleDurationTime.toLong()
            scaleXAnimator.doOnStart {
                rippleView.show()
            }
            animatorList.add(scaleXAnimator)
            val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 0.57f, rippleScale)
            scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleYAnimator.repeatMode = ObjectAnimator.RESTART
            scaleYAnimator.startDelay = (i * rippleDelay).toLong()
            scaleYAnimator.duration = rippleDurationTime.toLong()
            animatorList.add(scaleYAnimator)
            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f)
            alphaAnimator.repeatCount = ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = ObjectAnimator.RESTART
            alphaAnimator.startDelay = (i * rippleDelay).toLong()
            alphaAnimator.duration = rippleDurationTime.toLong()
            animatorList.add(alphaAnimator)
        }
        animatorSet.playTogether(animatorList)
    }

    fun onDestroyView() {
        removeAllViews()
        rippleViewList.clear()
        animatorList.clear()
        animatorSet.cancel()
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning()) {
            animatorSet.start()
            animationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning()) {
            animatorSet.end()
            animationRunning = false
        }
    }

    private fun isRippleAnimationRunning(): Boolean {
        return animationRunning
    }

    inner class RippleView(context: Context?) : View(context) {
        private val mRadius = 62f.toPixel
        private val mRectF = RectF()

        init {
            this.visibility = INVISIBLE
        }

        override fun onDraw(canvas: Canvas?) {
            mRectF.set(0f, 0f, width.toFloat(), height.toFloat())
            canvas?.drawRoundRect(
                mRectF,
                mRadius,
                mRadius,
                paint
            )
        }
    }

    companion object {
        private const val DEFAULT_RIPPLE_COUNT = 3
        private const val DEFAULT_DURATION_TIME = 3000
        private const val DEFAULT_SCALE = 1.0f
        private const val DEFAULT_FILL_TYPE = 0
    }

}
