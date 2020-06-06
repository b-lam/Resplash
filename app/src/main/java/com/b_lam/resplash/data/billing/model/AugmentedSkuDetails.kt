package com.b_lam.resplash.data.billing.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AugmentedSkuDetails(
    val canPurchase: Boolean, /* Not in SkuDetails; it's the augmentation */
    @PrimaryKey val sku: String,
    val type: String?,
    val title: String?,
    val description: String?,
    val price: String?,
    val priceAmountMicros: Long?,
    val originalJson: String?
)