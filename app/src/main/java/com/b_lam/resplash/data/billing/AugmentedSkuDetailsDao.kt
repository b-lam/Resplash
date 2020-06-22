package com.b_lam.resplash.data.billing

import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails

@Dao
interface AugmentedSkuDetailsDao {

    @Query("SELECT * FROM AugmentedSkuDetails WHERE type = '${BillingClient.SkuType.INAPP}'")
    fun getInappSkuDetailsLiveData(): LiveData<List<AugmentedSkuDetails>>

    @Query("SELECT * FROM AugmentedSkuDetails WHERE sku IN (:skuList)")
    fun getSkuDetailsLiveDataInList(skuList: List<String>): LiveData<List<AugmentedSkuDetails>>

    @Query("SELECT * FROM AugmentedSkuDetails WHERE sku = :sku")
    fun getSkuDetailsLiveDataById(sku: String): LiveData<AugmentedSkuDetails?>

    @Transaction
    fun insertOrUpdate(skuDetails: SkuDetails) = skuDetails.apply {
        val result = getById(sku)
        val bool = result?.canPurchase ?: true
        val originalJson = toString().substring("SkuDetails: ".length)
        val detail = AugmentedSkuDetails(
            bool,
            sku,
            type,
            title,
            description,
            price,
            priceAmountMicros,
            originalJson
        )
        insert(detail)
    }

    @Transaction
    fun insertOrUpdate(sku: String, canPurchase: Boolean) {
        val result = getById(sku)
        if (result != null) {
            update(sku, canPurchase)
        } else {
            insert(
                AugmentedSkuDetails(
                    canPurchase,
                    sku,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        }
    }

    @Query("SELECT * FROM AugmentedSkuDetails WHERE sku = :sku")
    fun getById(sku: String): AugmentedSkuDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(augmentedSkuDetails: AugmentedSkuDetails)

    @Query("UPDATE AugmentedSkuDetails SET canPurchase = :canPurchase WHERE sku = :sku")
    fun update(sku: String, canPurchase: Boolean)
}