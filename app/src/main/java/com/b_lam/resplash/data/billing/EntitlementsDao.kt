package com.b_lam.resplash.data.billing

import androidx.lifecycle.LiveData
import androidx.room.*
import com.b_lam.resplash.data.billing.model.Donation
import com.b_lam.resplash.data.billing.model.Entitlement
import com.b_lam.resplash.data.billing.model.ResplashPro

@Dao
interface EntitlementsDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(resplashPro: ResplashPro)

    @Update
    fun update(resplashPro: ResplashPro)

    @Query("SELECT * FROM resplash_pro LIMIT 1")
    fun getResplashPro(): LiveData<ResplashPro?>

    @Delete
    fun delete(resplashPro: ResplashPro)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(donationLevel: Donation)

    @Update
    fun update(donationLevel: Donation)

    @Query("SELECT * FROM donation LIMIT 1")
    fun getDonation(): LiveData<Donation?>

    @Delete
    fun delete(donationLevel: Donation)

    @Transaction
    fun insert(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> insert(it)
                is ResplashPro -> insert(it)
            }
        }
    }

    @Transaction
    fun update(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> update(it)
                is ResplashPro -> update(it)
            }
        }
    }

    @Transaction
    fun delete(vararg entitlements: Entitlement) {
        entitlements.forEach {
            when (it) {
                is Donation -> delete(it)
                is ResplashPro -> delete(it)
            }
        }
    }
}