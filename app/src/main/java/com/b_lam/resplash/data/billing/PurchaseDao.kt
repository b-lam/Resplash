package com.b_lam.resplash.data.billing

import androidx.room.*
import com.android.billingclient.api.Purchase
import com.b_lam.resplash.data.billing.model.CachedPurchase

@Dao
interface PurchaseDao {

    @Query("SELECT * FROM purchase_table")
    fun getPurchases(): List<CachedPurchase>

    @Insert
    fun insert(purchase: CachedPurchase)

    @Transaction
    fun insert(vararg purchases: Purchase) {
        purchases.forEach { insert(
            CachedPurchase(
                data = it
            )
        ) }
    }

    @Delete
    fun delete(vararg purchases: CachedPurchase)

    @Query("DELETE FROM purchase_table")
    fun deleteAll()

    @Query("DELETE FROM purchase_table WHERE data = :purchase")
    fun delete(purchase: Purchase)
}