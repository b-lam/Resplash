package com.b_lam.resplash.ui.donation

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.android.billingclient.api.ProductDetails
import com.b_lam.resplash.databinding.ItemDonationBinding

class DonationViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemDonationBinding by viewBinding()

    fun bind(
        productDetails: ProductDetails,
        callback: DonationAdapter.ItemEventCallback
    ) {
        with(binding) {
            skuTitleTextView.text = productDetails.title.dropLastWhile { it != '(' }.dropLast(1)
            skuDescriptionTextView.text = productDetails.description
            skuPriceTextView.text =
                productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "Unavailable"
            itemView.setOnClickListener { callback.onProductDetailsClick(productDetails) }
        }
    }
}