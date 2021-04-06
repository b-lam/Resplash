package com.b_lam.resplash.ui.photo.zoom

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }

        val url = intent.getStringExtra(EXTRA_PHOTO_URL)

        url?.let {
            binding.zoomImageView.loadPhotoUrl(it)
        }  ?: run {
            toast(R.string.oops)
            finish()
        }
    }

    companion object {

        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }
}
