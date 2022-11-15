package com.testarossa.template.library.android.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.testarossa.template.library.android.R
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan

abstract class BaseDialog<BD : ViewDataBinding> : DialogFragment() {
    // region Const and Fields
    protected lateinit var binding: BD
    private var restore = false
    // endregion

    // region override function
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                requestFeature(Window.FEATURE_NO_TITLE)
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = cancelable()
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(this, onBackPressedCallback)
        initViews()
    }

    override fun onStop() {
        restore = true
        super.onStop()
    }

    @Suppress("DEPRECATION")
    override fun onStart() {
        super.onStart()
        if (restoreAnimation()) {
            if (restore) {
                restore = false
                dialog?.window?.setWindowAnimations(
                    R.style.DialogAnimation_Restore
                )
            } else {
                dialog?.window?.setWindowAnimations(
                    R.style.DialogAnimation
                )
            }
        }

        if (fullScreen()) {
            dialog?.window?.apply {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                    setDecorFitsSystemWindows(false)
                    insetsController?.hide(WindowInsets.Type.statusBars())
                    insetsController?.show(WindowInsets.Type.navigationBars())
                    insetsController?.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
                }
                this.statusBarColor = Color.TRANSPARENT
            }
        }
    }
    // endregion

    // region abstract function
    @LayoutRes
    abstract fun getLayoutId(): Int
    // endregion

    // region protected function
    protected open fun handleBackPress() {

    }

    protected open fun cancelable(): Boolean = true

    protected open fun fullScreen() = false

    protected open fun restoreAnimation() = false

    protected open fun initViews() {

    }

    protected open fun navigateUp(id: Int, executeMethod: () -> Unit = {}) {
        if (findNavController().currentDestination?.id == id) {
            executeMethod()
            findNavController().navigateUp()
        }
    }

    protected open fun popBackStack(id: Int, executeMethod: () -> Unit = {}) {
        if (findNavController().currentDestination?.id == id) {
            executeMethod()
            findNavController().popBackStack()
        }
    }

    protected open fun navigate(id: Int, action: NavDirections) {
        if (findNavController().currentDestination?.id == id) {
            findNavController().navigate(action)
        }
    }
    // endregion
}
