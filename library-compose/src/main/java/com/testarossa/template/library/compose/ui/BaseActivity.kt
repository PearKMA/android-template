package com.testarossa.template.library.compose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate

abstract class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBeforeCreateViews(savedInstanceState)
        if (isSingleTask()) {
            if (!isTaskRoot) {
                finish()
                return
            }
        }
        if (!enableDarkMode()) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        initViews(savedInstanceState)
    }

    // region protected function
    protected open fun initBeforeCreateViews(savedInstanceState: Bundle?) {

    }

    protected open fun initViews(savedInstanceState: Bundle?) {

    }
    // endregion

    // region open function
    open fun isSingleTask(): Boolean = true

    open fun enableDarkMode(): Boolean = false
    // endregion

}
