package com.b_lam.resplash.ui.donation

import android.os.Bundle
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.loadBlurredImage
import com.b_lam.resplash.util.setupActionBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_donate.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class DonationActivity : BaseActivity(), DonationAdapter.ItemEventCallback {

    override val viewModel: DonationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.support_development)
            setDisplayHomeAsUpEnabled(true)
        }

        val donationAdapter = DonationAdapter(this)

        recycler_view.apply {
            adapter = donationAdapter
            layoutManager = LinearLayoutManager(this@DonationActivity).apply {
                addItemDecoration(SpacingItemDecoration(this@DonationActivity, R.dimen.keyline_7))
            }
        }

        viewModel.bannerPhotoLiveData.observe(this) {
            banner_image_view.loadBlurredImage(it.urls.small, it.color)
        }

        viewModel.skuDetailsLiveData.observe(this) { skuDetailsList ->
            donationAdapter.submitList(skuDetailsList.sortedBy { it.priceAmountMicros })
        }

        viewModel.purchaseCompleteLiveData.observeEvent(this) { showThanksDialog() }
    }

    private fun showThanksDialog() {
        val view = layoutInflater.inflate(R.layout.thanks_dialog_layout, null)
        val animationView = view.findViewById<LottieAnimationView>(R.id.trophy_animation_view)
        animationView.setOnClickListener { animationView.playAnimation() }
        MaterialAlertDialogBuilder(this)
            .setView(view)
            .setPositiveButton(R.string.you_are_welcome) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    override fun onSkuDetailsClick(augmentedSkuDetails: AugmentedSkuDetails) {
        viewModel.makePurchase(this, augmentedSkuDetails)
    }
}
