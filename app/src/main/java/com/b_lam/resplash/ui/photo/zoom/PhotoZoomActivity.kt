package com.b_lam.resplash.ui.photo.zoom

import android.os.Bundle
import android.view.View
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.loadPhotoUrl
import com.b_lam.resplash.util.toast
import kotlinx.android.synthetic.main.activity_photo_zoom.*


class PhotoZoomActivity : BaseActivity() {

    override val viewModel: BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_photo_zoom)

        val url = intent.getStringExtra(EXTRA_PHOTO_URL)

        url?.let {
            zoom_image_view.loadPhotoUrl(it)
        }  ?: run {
            toast(R.string.oops)
            finish()
        }
    }

    companion object {

        const val EXTRA_PHOTO_URL = "extra_photo_url"
    }
}
