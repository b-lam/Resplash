package com.b_lam.resplash.ui.donation

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails
import kotlinx.android.synthetic.main.item_donation.view.*

class DonationViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

    fun bind(
        skuDetails: AugmentedSkuDetails,
        callback: DonationAdapter.ItemEventCallback
    ) {
        with(itemView) {
            sku_title_text_view.text = skuDetails.title?.dropLastWhile { it != '(' }?.dropLast(1)
            sku_description_text_view.text = skuDetails.description
            sku_price_text_view.text = skuDetails.price
            setOnClickListener { callback.onSkuDetailsClick(skuDetails) }
        }
    }
}