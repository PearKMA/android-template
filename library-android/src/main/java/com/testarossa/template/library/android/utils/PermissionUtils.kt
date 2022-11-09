package com.testarossa.template.library.android.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.testarossa.template.library.android.ui.fragment.BaseFragment
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan
import com.testarossa.template.library.android.utils.extension.powerManager

fun Context.goToSettingsApplication() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .setData(Uri.fromParts("package", packageName, null))
    startActivity(intent)
}

fun showDialogPermission(context: Activity, title_id: Int, contentId: Int) {
    val dialogPermission: AlertDialog?

    val builder = AlertDialog.Builder(context)
    builder.setTitle(context.getString(title_id))
    builder.setMessage(context.getString(contentId))
    builder.setCancelable(true)
    builder.setPositiveButton(
        "Go to settings"
    ) { _, _ ->
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setData(Uri.fromParts("package", context.packageName, null))
        context.startActivity(intent)
    }

    /*builder.setNegativeButton(
        "Exit"
    ) { _, _ ->
        dialogPermission?.dismiss()
    }*/

    dialogPermission = builder.create()

    if (!context.isFinishing) {
        dialogPermission.show()
    }
}


fun Context.checkPermissionsGranted(list: List<String>): Boolean {
    list.forEach { permission ->
        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
            if (!isBuildLargerThan(Build.VERSION_CODES.Q) && ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
    }
    return true
}

fun BaseFragment<*>.checkPermissions(
    permissions: List<String>,
    title_id: Int,
    contentId: Int,
    onGrant: () -> Unit = {},
    onShowDialog: (() -> Unit)? = null,
) {
    this.doRequestPermission(permissions, {
        onGrant()
    }, {
        if (!requireActivity().isFinishing) {
            try {
                if (onShowDialog == null) {
                    showDialogPermission(
                        requireActivity(),
                        title_id,
                        contentId
                    )
                } else {
                    onShowDialog.invoke()
                }
            } catch (e: Exception) {

            }
        }
    })
}

fun Context.checkDrawOverlayPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}

fun Activity.enableDrawOverlayPermission() {
    val intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + this.packageName)
        )
    try {
        startActivity(intent)
    } catch (e: Exception) {
    }
}

fun Context.checkIgnoreBatteryOptimizations(): Boolean {
    return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
}

@SuppressLint("BatteryLife")
fun Context.requestIgnoringBatteryOptimizations() {
    try {
        val intent =
            Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
        startActivity(intent)
    } catch (e: Exception) {
    }
}
