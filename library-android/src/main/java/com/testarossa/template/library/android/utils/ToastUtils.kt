package com.testarossa.template.library.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan

object ToastUtils {
    private var toast: Toast? = null

    /**
     * Hiện thông báo lên màn hình
     */
    @Suppress("DEPRECATION")
    @SuppressLint("ShowToast")
    fun Context.showToast(message: String) {
        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            when {
                toast == null -> {
                    // Create toast if found null, it would he the case of first call only
                    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                toast!!.view == null -> {
                    // Toast not showing, so create new one
                    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                else -> {
                    // Updating toast message is showing
                    toast!!.setText(message)
                }
            }
        }
        // Showing toast finally
        toast?.show()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ShowToast")
    fun Context.showToast(stringId: Int) {
        val message = getString(stringId)
        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        } else {
            when {
                toast == null -> {
                    // Create toast if found null, it would he the case of first call only
                    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                toast!!.view == null -> {
                    // Toast not showing, so create new one
                    toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
                }

                else -> {
                    // Updating toast message is showing
                    toast!!.setText(message)
                }
            }
        }
        // Showing toast finally
        toast?.show()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ShowToast")
    fun Context.showLongToast(message: String) {
        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
            toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        } else {
            when {
                toast == null -> {
                    // Create toast if found null, it would he the case of first call only
                    toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                }

                toast!!.view == null -> {
                    // Toast not showing, so create new one
                    toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                }

                else -> {
                    // Updating toast message is showing
                    toast!!.setText(message)
                }
            }
        }
        // Showing toast finally
        toast?.show()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ShowToast")
    fun Context.showLongToast(stringId: Int) {
        val message = getString(stringId)
        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
            toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        } else {
            when {
                toast == null -> {
                    // Create toast if found null, it would he the case of first call only
                    toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                }

                toast!!.view == null -> {
                    // Toast not showing, so create new one
                    toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                }

                else -> {
                    // Updating toast message is showing
                    toast!!.setText(message)
                }
            }
        }
        // Showing toast finally
        toast?.show()
    }

    fun checkToastNull() = toast == null

    /**
     * Xóa thông báo
     */
    fun killToast() {
        toast?.cancel()
        toast = null
    }
}
