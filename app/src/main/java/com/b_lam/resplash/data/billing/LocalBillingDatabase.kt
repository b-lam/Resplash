package com.b_lam.resplash.data.billing

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.b_lam.resplash.data.billing.model.Donation
import com.b_lam.resplash.data.billing.model.ResplashPro

@Database(
    entities = [
        Donation::class,
        ResplashPro::class
    ],
    version = 2,
    autoMigrations = [
        AutoMigration (from = 1, to = 2, spec = LocalBillingDatabase.BillingV5AutoMigration::class)
    ]
)
abstract class LocalBillingDatabase : RoomDatabase() {
    abstract fun entitlementsDao(): EntitlementsDao

    @DeleteTable(tableName = "AugmentedSkuDetails")
    @DeleteTable(tableName = "purchase_table")
    class BillingV5AutoMigration : AutoMigrationSpec

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
