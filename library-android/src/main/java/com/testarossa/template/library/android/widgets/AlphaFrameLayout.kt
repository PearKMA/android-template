package com.testarossa.template.library.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class AlphaFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    private var onClickView: OnAlphaViewListener? = null

    fun setOnAlphaLayoutListener(onClickView: OnAlphaViewListener) {
        this.onClickView = onClickView
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (null != event) {
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    alpha = 1f
                    onClickView?.onClick(this)
                }

                MotionEvent.ACTION_CANCEL -> {
                    alpha = 1f
                }

                else -> {
                    alpha = 0.5f
                }
            }
        }
        return true
    }
}
