package com.testarossa.template.library.android.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.testarossa.template.library.android.utils.NotchUtils.getInternalDimensionSize
import com.testarossa.template.library.android.utils.extension.*
import com.testarossa.template.library.android.utils.loadBackground
import com.testarossa.template.library.android.utils.loadNormal
import com.testarossa.template.library.android.utils.loadRoundedCorner


private const val DEBOUNCE_CLICK_INTERVAL = 350L
private const val DEBOUNCE_LONG_CLICK_INTERVAL = 750L

@BindingAdapter("debounceClick")
fun View.onDebounceClick(listener: View.OnClickListener) {
    this.onDebounceClick(DEBOUNCE_CLICK_INTERVAL) {
        listener.onClick(this)
    }
}

@BindingAdapter("debounceLongClick")
fun View.onDebounceLongClick(listener: View.OnClickListener) {
    this.onDebounceClick(DEBOUNCE_LONG_CLICK_INTERVAL) {
        listener.onClick(this)
    }
}

@Suppress("DEPRECATION")
@BindingAdapter("paddingTop")
fun View.setPaddingTop(space: Float) {
    if (isBuildLargerThan(buildVersion.P)) {
        this.setOnApplyWindowInsetsListener { _, windowInsets ->
            val safeHeight = windowInsets.displayCutout?.safeInsetTop ?: 0
            val actionBarSize = safeHeight + space
            this.setPadding(0, actionBarSize.toInt(), 0, 0)
            if (this.layoutParams is ConstraintLayout.LayoutParams) {
                val params = this.layoutParams as ConstraintLayout.LayoutParams
                val tv = TypedValue()
                context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)
                val actionBarHeight = context.resources.getDimensionPixelSize(tv.resourceId)
                params.height = actionBarHeight + actionBarSize.toInt()
            }
            windowInsets
        }
    }
}

@Suppress("DEPRECATION")
@BindingAdapter("marginTop")
fun View.setMarginTop(space: Float) {
    if (isBuildLargerThan(buildVersion.P)) {
        this.setOnApplyWindowInsetsListener { _, windowInsets ->
            val safeHeight = windowInsets.displayCutout?.safeInsetTop ?: 0
            val actionBarSize = safeHeight + space

            val params = this.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, actionBarSize.toInt(), 0, 0)

            /*if (isBuildLargerThan(buildVersion.R)) {
                WindowInsets.CONSUMED
            } else {
                windowInsets.consumeDisplayCutout()
            }*/
            windowInsets
        }
    } else if (this.layoutParams is ConstraintLayout.LayoutParams) {
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        val statusBarHeight = this.context.getInternalDimensionSize(
            STATUS_BAR_HEIGHT
        )

        if (isBuildLargerThan(buildVersion.R) && statusBarHeight > 0) {
            val actionBarSize = statusBarHeight + space
            params.setMargins(0, actionBarSize.toInt(), 0, 0)
        } else {
            params.setMargins(0, space.toInt(), 0, 0)
        }
    }
}

@BindingAdapter("marginBottom")
fun View.setMarginBottom(space: Float) {
    if (isBuildLargerThan(buildVersion.P)) {
        this.setOnApplyWindowInsetsListener { _, windowInsets ->
            val safeHeight = windowInsets.displayCutout?.safeInsetTop ?: 0
            val actionBarSize = safeHeight + space

            val params = this.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, 0, actionBarSize.toInt())

            /*if (isBuildLargerThan(buildVersion.R)) {
                WindowInsets.CONSUMED
            } else {
                windowInsets.consumeDisplayCutout()
            }*/
            windowInsets
        }
    } else if (this.layoutParams is ConstraintLayout.LayoutParams) {
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        val navigationBarHeight = this.context.getInternalDimensionSize(
            NAVIGATION_BAR_HEIGHT
        )
        if (isBuildLargerThan(buildVersion.R) && navigationBarHeight > 0) {
            val actionBarSize = navigationBarHeight + space
            params.setMargins(0, 0, 0, actionBarSize.toInt())
        } else {
            params.setMargins(0, 0, 0, space.toInt())
        }
    }
}

@BindingAdapter("loadBackground")
fun View.loadImageAsBackground(source: Any?) {
    source?.let {
        loadBackground(it)
    }
}

@BindingAdapter("loadRoundedImage", "cornerImage")
fun ImageView.loadRoundedImage(source: Any?, corner: Float = 0f) {
    source?.let {
        if (corner > 0f)
            loadRoundedCorner(it, corner.toInt())
        else
            loadNormal(it)
    }
}

@BindingAdapter("loadCenterCrop")
fun ImageView.loadCenterCrop(source: Any?) {
    source?.let {
        loadCenterCrop(source)
    }
}

@BindingAdapter("tint")
fun ImageView.setImageTint(color: String?) {
    if (color != null) {
        try {
            setColorFilter(Color.parseColor(color))
        } catch (e: NumberFormatException) {
            clearColorFilter()
        }
    }
}

@BindingAdapter("textDrawableStart", "textDrawableTop", "textDrawableEnd", "textDrawableBottom")
fun TextView.setTextDrawable(
    drawStart: Drawable?,
    drawTop: Drawable?,
    drawEnd: Drawable?,
    drawBottom: Drawable?
) {
    setCompoundDrawablesWithIntrinsicBounds(drawStart, drawTop, drawEnd, drawBottom)
}
