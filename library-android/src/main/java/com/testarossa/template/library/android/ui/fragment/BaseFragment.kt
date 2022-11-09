package com.testarossa.template.library.android.ui.fragment

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.testarossa.template.library.android.utils.ToastUtils.killToast
import com.testarossa.template.library.android.utils.checkPermissionsGranted
import com.testarossa.template.library.android.utils.extension.isBuildLargerThan


enum class TypeScreen {
    FULL_SCREEN, NORMAL_SCREEN, TRANSLUCENT_STATUS_BAR, NO_LIMIT, NO_LIMIT_WITH_STATUS_BAR, NONE
}

abstract class BaseFragment<BD : ViewDataBinding> : Fragment() {
    // region Const and Fields
    private var _binding: BD? = null
    protected val binding: BD
        get() = _binding!!

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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScreenType()
        initBackPress()
        initViews(savedInstanceState)
        observableData()
    }

    override fun onPause() {
        killToast()
        initPause()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region abstract function
    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun getViewId(): Int
    // endregion

    // region open function
    open fun initViews(savedInstanceState: Bundle?) {}

    open fun observableData() {}

    open fun handleBackPressed() {}

    open fun typeScreen(): TypeScreen = TypeScreen.NONE

    open fun isDarkTheme(): Boolean = false

    open fun getStatusBarColor(): Int = Color.TRANSPARENT

    open fun initPause() {}

    open fun onActivityReturned(result: ActivityResult) {}

    open fun doRequestPermission(
        permissions: List<String>,
        onAllow: () -> Unit = {}, onDenied: (Boolean) -> Unit = {}
    ) {
        this.onAllow = onAllow
        this.onDenied = onDenied

        if (requireContext().checkPermissionsGranted(permissions)) {
            onAllow()
        } else {
            requestPermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
    // endregion

    // region protected method

    protected fun onStartActivityForResult(intent: Intent, option: ActivityOptionsCompat? = null) {
        resultLauncher.launch(intent, option)
    }

    protected fun onNavigateUp() {
        if (findNavController().currentDestination?.id == getViewId()) {
            findNavController().navigateUp()
        }
    }

    protected fun onNavigate(deepLink: Int, bundle: Bundle? = null) {
        if (findNavController().currentDestination?.id == getViewId()) {
            if (bundle == null) {
                findNavController().navigate(deepLink)
            } else {
                findNavController().navigate(deepLink, bundle)
            }
        }
    }

    protected fun onNavigate(action: NavDirections) {
        if (findNavController().currentDestination?.id == getViewId()) {
            findNavController().navigate(action)
        }
    }

    protected fun navigateWithClearStack(destination: Int) {
        val current = findNavController().currentDestination?.id
        if (null != current) {
            findNavController().popBackStack(current, true)
            if (current != destination) {
                findNavController().navigate(destination)
            }
        }
    }
    // endregion

    // region private function
    private fun initBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPressed()
                }
            })
    }

    /**
     * @value TypeScreen#NO_LIMIT: Full screen, no status bar and navigation bar
     * @value TypeScreen#FULL_SCREEN: android 11 -> please set margin above navigation bar and below status bar
     * @value TypeScreen#TypeScreen.NORMAL_SCREEN: android 11 -> There's no need to set margin
     * @value TypeScreen#TRANSLUCENT_STATUS_BAR: similar FullScreen on Android 11
     * @value TypeScreen#NONE: default, no change settings from last config
     * */
    @Suppress("DEPRECATION")
    protected fun setScreenType() {
        when (typeScreen()) {
            TypeScreen.NO_LIMIT -> {
                activity?.window?.apply {
                    if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                        setDecorFitsSystemWindows(false)
                        insetsController?.hide(WindowInsets.Type.statusBars())
                        insetsController?.hide(WindowInsets.Type.navigationBars())
                        insetsController?.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    } else {
                        addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                // Hide the nav bar and status bar
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN)
                    }
                    this.statusBarColor = this@BaseFragment.getStatusBarColor()
                }
            }

            TypeScreen.NO_LIMIT_WITH_STATUS_BAR -> {
                activity?.window?.apply {
                    if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                        setDecorFitsSystemWindows(false)
                        insetsController?.hide(WindowInsets.Type.navigationBars())
                        insetsController?.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        if (isDarkTheme()) {
                            decorView.windowInsetsController?.setSystemBarsAppearance(
                                0, APPEARANCE_LIGHT_STATUS_BARS
                            )
                        } else {
                            decorView.windowInsetsController?.setSystemBarsAppearance(
                                APPEARANCE_LIGHT_STATUS_BARS,
                                APPEARANCE_LIGHT_STATUS_BARS
                            )
                        }
                    } else {
                        addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                        val systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                // Hide the nav bar and status bar
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN)
                        decorView.systemUiVisibility = if (isDarkTheme()) {
                            systemUiVisibility and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
                        } else {
                            systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                    }
                    this.statusBarColor = this@BaseFragment.getStatusBarColor()
                }
            }

            TypeScreen.FULL_SCREEN -> {
                activity?.window?.apply {
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
                    this.statusBarColor = this@BaseFragment.getStatusBarColor()
                }
            }

            TypeScreen.NORMAL_SCREEN -> {
                activity?.window?.apply {
                    if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                        setDecorFitsSystemWindows(true)
                        insetsController?.show(WindowInsets.Type.statusBars())
                        insetsController?.show(WindowInsets.Type.navigationBars())
                        insetsController?.systemBarsBehavior =
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        if (isDarkTheme()) {
                            decorView.windowInsetsController?.setSystemBarsAppearance(
                                0, APPEARANCE_LIGHT_STATUS_BARS
                            )
                        } else {
                            decorView.windowInsetsController?.setSystemBarsAppearance(
                                APPEARANCE_LIGHT_STATUS_BARS,
                                APPEARANCE_LIGHT_STATUS_BARS
                            )
                        }
                        this.statusBarColor = this@BaseFragment.getStatusBarColor()
                    } else {
                        clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                        clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                        decorView.systemUiVisibility = if (isDarkTheme()) {
                            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()) and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
                        } else {
                            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE and View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN.inv()) or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        }
                        this.statusBarColor = this@BaseFragment.getStatusBarColor()
                    }

                }
            }

            TypeScreen.TRANSLUCENT_STATUS_BAR -> {
                if (isBuildLargerThan(Build.VERSION_CODES.M)) {
                    activity?.window?.apply {
                        if (isBuildLargerThan(Build.VERSION_CODES.R)) {
                            setDecorFitsSystemWindows(false)
                            insetsController?.show(WindowInsets.Type.statusBars())
                            insetsController?.show(WindowInsets.Type.navigationBars())
                            insetsController?.systemBarsBehavior =
                                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            if (isDarkTheme()) {
                                decorView.windowInsetsController?.setSystemBarsAppearance(
                                    0, APPEARANCE_LIGHT_STATUS_BARS
                                )
                            } else {
                                decorView.windowInsetsController?.setSystemBarsAppearance(
                                    APPEARANCE_LIGHT_STATUS_BARS,
                                    APPEARANCE_LIGHT_STATUS_BARS
                                )
                            }
                            this.statusBarColor = this@BaseFragment.getStatusBarColor()
                        } else {
                            clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                            clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                            decorView.systemUiVisibility = if (isDarkTheme()) {
                                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) and (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).inv()
                            } else {
                                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            }
                            this.statusBarColor = this@BaseFragment.getStatusBarColor()
                        }
                    }

                }
            }

            TypeScreen.NONE -> {
            }
        }
    }
    // endregion
}
