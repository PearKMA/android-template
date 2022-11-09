package com.testarossa.template.library.android.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.testarossa.template.library.android.utils.checkPermissionsGranted
import com.testarossa.template.library.android.utils.extension.inputMethodManager
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan

abstract class BaseActivity<BD : ViewDataBinding> : AppCompatActivity() {
    // region Const and Fields
    protected lateinit var binding: BD

    // request permission
    private var onAllow: (() -> Unit)? = null
    private var onDenied: ((Boolean) -> Unit)? =
        null // true: can continue request, false: open settings

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var granted = true
            var continueRequest = true
            permissions.entries.forEach { entry ->
                if (!entry.value) {
                    if (entry.key == Manifest.permission.WRITE_EXTERNAL_STORAGE && isBuildLargerThan(
                            Build.VERSION_CODES.Q
                        )
                    ) {
                        // can ignore because from Android Q, WRITE_EXTERNAL_STORAGE permission always be false
                    } else {
                        granted = false
                        if (continueRequest) {
                            continueRequest = shouldShowRequestPermissionRationale(entry.key)
                        }
                    }
                }
            }
            if (granted) {
                onAllow?.invoke()
            } else {
                onDenied?.invoke(continueRequest)
            }
        }

    // other request
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityReturned(result)
        }
    // endregion

    // region override function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBeforeCreateViews(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        if (isSingleTask()) {
            if (!isTaskRoot) {
                finish()
                return
            }
        }
        if (!enableDarkMode()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initViews(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverrideConfiguration = Configuration(newBase?.resources?.configuration)
            .apply { fontScale = 1.0f }
        applyOverrideConfiguration(newOverrideConfiguration)
        super.attachBaseContext(newBase)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val rect = Rect()
                if (!rect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    val token = v.windowToken
                    if (token != null) {
                        v.clearFocus()
                        inputMethodManager?.hideSoftInputFromWindow(token, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    // endregion

    // region abstract function
    @LayoutRes
    abstract fun getLayoutId(): Int
    // endregion

    // region protected function
    protected open fun initBeforeCreateViews(savedInstanceState: Bundle?) {

    }

    protected open fun initViews(savedInstanceState: Bundle?) {

    }

    protected fun onStartActivityForResult(intent: Intent, option: ActivityOptionsCompat? = null) {
        resultLauncher.launch(intent, option)
    }
    // endregion

    // region open function
    open fun isSingleTask(): Boolean = false

    open fun enableDarkMode(): Boolean = false

    open fun onActivityReturned(result: ActivityResult) {}

    open fun doRequestPermission(
        permissions: List<String>,
        onAllow: () -> Unit = {}, onDenied: (Boolean) -> Unit = {}
    ) {
        this.onAllow = onAllow
        this.onDenied = onDenied

        if (checkPermissionsGranted(permissions)) {
            onAllow()
        } else {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
    // endregion
}