package com.b_lam.resplash.ui.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.android.billingclient.api.ProductDetails
import com.b_lam.resplash.R

class DonationAdapter(
    private val callback: ItemEventCallback
) : ListAdapter<ProductDetails, DonationViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        holder.bind(getItem(position), callback)
    }

    interface ItemEventCallback {

        fun onProductDetailsClick(productDetails: ProductDetails)
    }

        companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<ProductDetails>() {

            override fun areItemsTheSame(oldItem: ProductDetails, newItem: ProductDetails) =
                oldItem.productId == newItem.productId

            override fun areContentsTheSame(oldItem: ProductDetails, newItem: ProductDetails) =
                oldItem == newItem
        }
    }
}