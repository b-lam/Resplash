package com.b_lam.resplash.data.billing

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.b_lam.resplash.data.billing.model.*

@Database(
    entities = [
        AugmentedSkuDetails::class,
        CachedPurchase::class,
        Donation::class,
        ResplashPro::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(PurchaseTypeConverter::class)
abstract class LocalBillingDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
    abstract fun entitlementsDao(): EntitlementsDao
    abstract fun skuDetailsDao(): AugmentedSkuDetailsDao

    companion object {

        @Volatile
        private var INSTANCE: LocalBillingDatabase? = null
        private const val DATABASE_NAME = "purchase_db"

        fun getInstance(context: Context): LocalBillingDatabase =
            INSTANCE
                ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(
                        context.applicationContext
                    ).also {
                    INSTANCE = it
                }
            }

        private fun buildDatabase(appContext: Context): LocalBillingDatabase {
            return Room.databaseBuilder(appContext, LocalBillingDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Data is cache, so it is OK to delete
                .build()
        }
    }
}