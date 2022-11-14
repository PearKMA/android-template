package com.testarossa.template.library.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.WindowManager
import com.testarossa.template.library.android.utils.extension.buildVersion
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan

val Number.toPixel
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

@SuppressLint("DiscouragedApi", "InternalInsetResource")
fun hasNotch(context: Context): Boolean {
    var statusBarHeight = 0
    val resourceId = context.resources.getIdentifier(
        "status_bar_height",
        "dimen",
        "android"
    )
    if (resourceId > 0) {
        statusBarHeight = context.resources.getDimensionPixelOffset(resourceId)
    }
    val barHeight = 24f.toPixel.toInt()

    return statusBarHeight > barHeight
}

val Context.screenWidth: Int
    get() {
        return if (isBuildLargerThan(buildVersion.R)) {
            getSystemService(WindowManager::class.java).currentWindowMetrics.bounds.width()
        } else {
            Resources.getSystem().displayMetrics.widthPixels
        }
    }

val Context.screenHeight: Int
    get() {
        return if (isBuildLargerThan(buildVersion.R)) {
            getSystemService(WindowManager::class.java).currentWindowMetrics.bounds.height()
        } else {
            Resources.getSystem().displayMetrics.heightPixels
        }
    }

val Context.actionBarSize
    get() = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        .let { attrs -> attrs.getDimension(0, 0F).also { attrs.recycle() } }