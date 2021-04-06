package com.b_lam.resplash.ui.donation

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails
import com.b_lam.resplash.databinding.ItemDonationBinding

class DonationViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    private val binding: ItemDonationBinding by viewBinding()

    fun bind(
        skuDetails: AugmentedSkuDetails,
        callback: DonationAdapter.ItemEventCallback
    ) {
        with(binding) {
            skuTitleTextView.text = skuDetails.title?.dropLastWhile { it != '(' }?.dropLast(1)
            skuDescriptionTextView.text = skuDetails.description
            skuPriceTextView.text = skuDetails.price
            itemView.setOnClickListener { callback.onSkuDetailsClick(skuDetails) }
        }
    }
}