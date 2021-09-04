package com.b_lam.resplash.ui.photo.zoom

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivityPhotoZoomBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.loadPhotoUrl
import com.b_lam.resplash.util.toast

class PhotoZoomActivity : BaseActivity(R.layout.activity_photo_zoom) {

    override val viewModel: ViewModel? = null

    override val binding: ActivityPhotoZoomBinding by viewBinding()

    private var isSystemUiVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        toggleSystemUI(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
                val suppliedInsets = view.onApplyWindowInsets(windowInsets)
                isSystemUiVisible = suppliedInsets.isVisible(
                    WindowInsetsCompat.Type.statusBars()
                            or WindowInsetsCompat.Type.navigationBars()
                )
                suppliedInsets
            }
        } else {
            window.decorView.setOnSystemUiVisibilityChangeListener {
                isSystemUiVisible = (it and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
            }
        }

        binding.zoomImageView.setOnClickListener {
            toggleSystemUI(!isSystemUiVisible)
        }

        val url = intent.getStringExtra(EXTRA_PHOTO_URL)

        url?.let {
            binding.zoomImageView.loadPhotoUrl(it)
        } ?: run {
            toast(R.string.oops)
            finish()
        }
    }

    private fun toggleSystemUI(showSystemUI: Boolean) {
        WindowInsetsControllerCompat(window, binding.root).let {
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showSystemUI) {
                it.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            } else {
                it.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    companion object {

        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }
}
