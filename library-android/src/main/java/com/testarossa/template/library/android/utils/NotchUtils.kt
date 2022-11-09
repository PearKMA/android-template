package com.testarossa.template.library.android.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.view.View
import com.testarossa.template.library.android.utils.extension.STATUS_BAR_HEIGHT
import com.testarossa.template.library.android.utils.extension.buildVersion
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import java.lang.reflect.InvocationTargetException


object NotchUtils {
    private const val NOTCH_HUA_WEI = "com.huawei.android.util.HwNotchSizeUtil"
    private const val NOTCH_OPPO = "com.oppo.feature.screen.heteromorphism"
    private const val NOTCH_VIVO = "android.util.FtFeature"
    private const val NOTCH_XIAO_MI = "ro.miui.notch"
    private const val NOTCH_LENOVO = "config_screen_has_notch"
    private const val NOTCH_MEIZU = "flyme.config.FlymeFeature"
    private const val SYSTEM_PROPERTIES = "android.os.SystemProperties"

    fun Activity?.hasNotchScreen(): Boolean {
        return if (this != null) {
            if (isBuildLargerThan(buildVersion.P)) {
                hasNotchAtAndroidP()
            } else {
                hasNotchAtXiaoMi() ||
                        hasNotchAtHuaWei() ||
                        hasNotchAtOPPO() ||
                        hasNotchAtVIVO() ||
                        hasNotchAtLenovo() ||
                        hasNotchAtMeiZu()
            }
        } else {
            false
        }
    }

    fun View?.hasNotchScreen(): Boolean {
        return if (this != null) {
            if (isBuildLargerThan(buildVersion.P)) {
                hasNotchAtAndroidP()
            } else {
                context.hasNotchAtXiaoMi() ||
                        context.hasNotchAtHuaWei() ||
                        context.hasNotchAtOPPO() ||
                        context.hasNotchAtVIVO()
            }
        } else {
            false
        }
    }

    private fun View?.hasNotchAtAndroidP(): Boolean {
        if (isBuildLargerThan(buildVersion.P)) {
            return this?.rootWindowInsets?.displayCutout != null
        }
        return false
    }

    private fun Activity?.hasNotchAtAndroidP(): Boolean {
        if (isBuildLargerThan(buildVersion.P)) {
            return this?.window?.decorView?.rootWindowInsets?.displayCutout != null
        }
        return false
    }

    @SuppressLint("PrivateApi")
    private fun Context?.hasNotchAtXiaoMi(): Boolean {
        var result = 0
        if (Build.MANUFACTURER.lowercase().contains("xiaomi")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(SYSTEM_PROPERTIES)
                val method = aClass?.getMethod(
                    "getInt", String::class.java,
                    Int::class.javaPrimitiveType
                )
                result = method?.invoke(aClass, NOTCH_XIAO_MI, 0) as Int
            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: IllegalAccessException) {

            } catch (ignored: InvocationTargetException) {

            } catch (ignored: ClassNotFoundException) {

            }
        }
        return result == 1
    }

    private fun Context?.hasNotchAtHuaWei(): Boolean {
        var result = false
        if (Build.MANUFACTURER.lowercase().contains("huawei")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(NOTCH_HUA_WEI)
                val method = aClass?.getMethod("hasNotchInScreen")
                result = method?.invoke(aClass) as Boolean
            } catch (ignored: ClassNotFoundException) {

            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: Exception) {

            }
        }
        return result
    }

    @SuppressLint("PrivateApi")
    private fun Context?.hasNotchAtVIVO(): Boolean {
        var result = false
        if (Build.MANUFACTURER.lowercase().contains("vivo")) {
            try {
                val classLoader = this?.classLoader
                val aClass = classLoader?.loadClass(NOTCH_VIVO)
                val method = aClass?.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
                result = method?.invoke(aClass, 0x00000020) as Boolean
            } catch (ignored: ClassNotFoundException) {

            } catch (ignored: NoSuchMethodException) {

            } catch (ignored: Exception) {

            }
        }
        return result
    }

    private fun Context?.hasNotchAtOPPO(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("oppo")) {
            return try {
                this?.packageManager?.hasSystemFeature(NOTCH_OPPO) ?: false
            } catch (ignored: Exception) {
                false
            }
        }
        return false
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.hasNotchAtLenovo(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("lenovo")) {
            val resourceId = this?.resources?.getIdentifier(NOTCH_LENOVO, "bool", "android") ?: 0
            if (resourceId > 0) {
                return this?.resources?.getBoolean(resourceId) ?: false
            }
        }
        return false
    }

    private fun Context?.hasNotchAtMeiZu(): Boolean {
        if (Build.MANUFACTURER.lowercase().contains("meizu")) {
            return try {
                val clazz = Class.forName(NOTCH_MEIZU)
                val field = clazz.getDeclaredField("IS_FRINGE_DEVICE")
                field.get(null) as Boolean
            } catch (e: Exception) {
                false
            }
        }
        return false
    }

    fun Activity?.getNotchHeight(): Int {
        if (this == null || !this.hasNotchScreen()) {
            return 0
        }

        var notchHeight = 0
        val statusBarHeight = getInternalDimensionSize(STATUS_BAR_HEIGHT)

        val displayCutout = if (isBuildLargerThan(buildVersion.P)) {
            this.window?.decorView?.rootWindowInsets?.displayCutout
        } else {
            null
        }
        if (isBuildLargerThan(buildVersion.P) && displayCutout != null) {
            notchHeight =
                if (this.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    displayCutout.safeInsetTop
                } else {
                    if (displayCutout.safeInsetLeft == 0) {
                        displayCutout.safeInsetRight
                    } else {
                        displayCutout.safeInsetLeft
                    }
                }
        } else {
            if (hasNotchAtXiaoMi()) {
                notchHeight = getXiaoMiNotchHeight()
            }

            if (hasNotchAtHuaWei()) {
                notchHeight = getHuaWeiNotchSize()[1]
            }
            if (hasNotchAtVIVO()) {
                notchHeight = 32f.toPixel.toInt()
                if (notchHeight < statusBarHeight) {
                    notchHeight = statusBarHeight
                }
            }
            if (hasNotchAtOPPO()) {
                notchHeight = 80
                if (notchHeight < statusBarHeight) {
                    notchHeight = statusBarHeight
                }
            }
            if (hasNotchAtLenovo()) {
                notchHeight = getLenovoNotchHeight()
            }
            if (hasNotchAtMeiZu()) {
                notchHeight = getMeizuNotchHeight()
            }
        }
        return notchHeight
    }

    fun View?.getNotchHeight(): Int {
        val activity = this?.context.getActivity() ?: return 0
        return activity.getNotchHeight()
    }

    private fun Context?.getActivity(): Activity? {
        if (this == null) return null
        else if (this is ContextWrapper) {
            return if (this is Activity) {
                this
            } else {
                this.baseContext.getActivity()
            }
        }
        return null
    }

    @SuppressLint("DiscouragedApi")
    fun Context.getInternalDimensionSize(key: String): Int {
        try {
            val resourceId =
                Resources.getSystem().getIdentifier(key, "dimen", "android")
            if (resourceId > 0) {
                val sizeOne: Int = resources.getDimensionPixelSize(resourceId)
                val sizeTwo: Int = Resources.getSystem().getDimensionPixelSize(resourceId)
                return if (sizeTwo >= sizeOne && !(isBuildLargerThan(buildVersion.Q) &&
                            key != STATUS_BAR_HEIGHT)
                ) {
                    sizeTwo
                } else {
                    val densityOne: Float = resources.displayMetrics.density
                    val densityTwo = Resources.getSystem().displayMetrics.density
                    val f = sizeOne * densityTwo / densityOne
                    (if (f >= 0) f + 0.5f else f - 0.5f).toInt()
                }
            }
        } catch (ignored: Resources.NotFoundException) {
            return 0
        }
        return 0
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getXiaoMiNotchHeight(): Int {
        val resourceId = this?.resources?.getIdentifier("notch_height", "dimen", "android") ?: 0
        return if (resourceId > 0) {
            this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        } else {
            0
        }
    }

    private fun Context?.getHuaWeiNotchSize(): IntArray {
        val ret = intArrayOf(0, 0)
        return try {
            val cl = this?.classLoader
            val aClass = cl?.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val get = aClass?.getMethod("getNotchSize")
            get?.invoke(aClass) as IntArray
        } catch (ignored: ClassNotFoundException) {
            ret
        } catch (ignored: NoSuchMethodException) {
            ret
        } catch (ignored: Exception) {
            ret
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getLenovoNotchHeight(): Int {
        val resourceId: Int = this?.resources?.getIdentifier("notch_h", "dimen", "android") ?: 0
        return if (resourceId > 0) {
            this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        } else {
            0
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Context?.getMeizuNotchHeight(): Int {
        var notchHeight = 0
        val resourceId: Int =
            this?.resources?.getIdentifier("fringe_height", "dimen", "android") ?: 0
        if (resourceId > 0) {
            notchHeight = this?.resources?.getDimensionPixelSize(resourceId) ?: 0
        }
        return notchHeight
    }
}
