package com.b_lam.resplash.ui.upgrade

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.airbnb.lottie.LottieAnimationView
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.loadBlurredImage
import com.b_lam.resplash.util.setupActionBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_upgrade.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpgradeActivity : BaseActivity() {

    override val viewModel: UpgradeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgrade)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.resplash_pro)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.bannerPhotoLiveData.observe(this) {
            banner_image_view.loadBlurredImage(it.urls.small, it.color)
        }

        viewModel.skuDetailsLiveData.observe(this) {
            go_pro_button.isVisible = it != null && it.canPurchase
        }

        viewModel.resplashProLiveData.observe(this) {
            if (it?.entitled == true) {
                showThanksDialog()
            }
        }

        go_pro_button.setOnClickListener { viewModel.makePurchase(this) }

        restore_purchase_button.setOnClickListener { viewModel.restorePurchase() }
    }

    private fun showThanksDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_thanks, null)
        val animationView = view.findViewById<LottieAnimationView>(R.id.trophy_animation_view)
        animationView.setOnClickListener { animationView.playAnimation() }
        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(R.string.you_are_welcome) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setOnDismissListener { finish() }
            .create()
            .show()
    }
}
