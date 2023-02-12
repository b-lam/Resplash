package com.b_lam.resplash.ui.donation

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.airbnb.lottie.LottieAnimationView
import com.android.billingclient.api.ProductDetails
import com.b_lam.resplash.R
import com.b_lam.resplash.data.billing.Sku
import com.b_lam.resplash.databinding.ActivityDonateBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.loadBlurredImage
import com.b_lam.resplash.util.setupActionBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class DonationActivity : BaseActivity(R.layout.activity_donate), DonationAdapter.ItemEventCallback {

    override val viewModel: DonationViewModel by viewModel()

    override val binding: ActivityDonateBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.support_development)
            setDisplayHomeAsUpEnabled(true)
        }

        val donationAdapter = DonationAdapter(this)

        binding.recyclerView.apply {
            adapter = donationAdapter
            layoutManager = LinearLayoutManager(this@DonationActivity).apply {
                addItemDecoration(SpacingItemDecoration(this@DonationActivity, R.dimen.keyline_7))
            }
        }

        viewModel.bannerPhotoLiveData.observe(this) {
            binding.bannerImageView.loadBlurredImage(it.urls.small, it.color)
        }

        viewModel.productDetailsLiveData.observe(this) { productDetails ->
            val sortedConsumableProductDetails = Sku.CONSUMABLE_PRODUCTS
                .mapNotNull { productDetails[it] }
                .sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros }
            donationAdapter.submitList(sortedConsumableProductDetails)
        }

        viewModel.purchaseCompleteLiveData.observeEvent(this) { showThanksDialog() }
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
            .create()
            .show()
    }

    override fun onProductDetailsClick(productDetails: ProductDetails) {
        viewModel.makePurchase(this, productDetails)
    }
}
