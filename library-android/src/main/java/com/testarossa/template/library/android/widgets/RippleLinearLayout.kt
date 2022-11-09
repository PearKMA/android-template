package com.testarossa.template.library.android.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class RippleLinearLayout : LinearLayout, IRippleClick {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setOnClickListener(l: OnClickListener?) {
        setBackground(context, this, background)
        super.setOnClickListener(l)
    }
}

