package com.b_lam.resplash.ui.upgrade

import android.os.Bundle
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.lottie.LottieAnimationView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.billing.Sku
import com.b_lam.resplash.databinding.ActivityUpgradeBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.loadBlurredImage
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpgradeActivity : BaseActivity(R.layout.activity_upgrade) {

    override val viewModel: UpgradeViewModel by viewModel()

    override val binding: ActivityUpgradeBinding by viewBinding()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAnalytics = Firebase.analytics

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.resplash_pro)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.bannerPhotoLiveData.observe(this) {
            binding.bannerImageView.loadBlurredImage(it.urls.small, it.color)
        }

        viewModel.productDetailsLiveData.observe(this) { productDetails ->
            binding.goProButton.isVisible = productDetails[Sku.RESPLASH_PRO] != null
        }

        viewModel.resplashProLiveData.observe(this) {
            if (it?.entitled == true) {
                showThanksDialog()
            }
        }

        binding.goProButton.setOnClickListener {
            observeBillingResponse()
            viewModel.makePurchase(this)
        }

        binding.restorePurchaseButton.setOnClickListener {
            observeBillingResponse()
            viewModel.restorePurchase()
        }
    }

    private fun observeBillingResponse() {
        viewModel.billingMessageLiveData.observeEvent(this) { toast(it) }
        viewModel.billingErrorLiveData.observeEvent(this) {
            firebaseAnalytics.logEvent("billing_error") {
                param("response_code", "${it.responseCode}")
                param("debug_message", it.debugMessage)
            }
        }
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
