package com.b_lam.resplash.ui.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.b_lam.resplash.R
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails

class DonationAdapter(
    private val callback: ItemEventCallback
) : ListAdapter<AugmentedSkuDetails, DonationViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }

    interface ItemEventCallback {

        fun onSkuDetailsClick(augmentedSkuDetails: AugmentedSkuDetails)
    }

        companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<AugmentedSkuDetails>() {
            override fun areItemsTheSame(oldItem: AugmentedSkuDetails, newItem: AugmentedSkuDetails) = oldItem.sku == newItem.sku
            override fun areContentsTheSame(oldItem: AugmentedSkuDetails, newItem: AugmentedSkuDetails) = oldItem == newItem
        }
    }
}