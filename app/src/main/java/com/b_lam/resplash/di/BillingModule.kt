package com.b_lam.resplash.di

import android.app.Application
import com.android.billingclient.api.BillingClient
import com.b_lam.resplash.domain.billing.BillingRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val billingModule = module {
    // Declare a single instance of BillingClient with the correct listener
    single {
        BillingClient.newBuilder(androidApplication())
            .setListener(get<BillingRepository>()) // Inject the BillingRepository as the listener
            .build()
    }

    // Declare the BillingRepository, passing the Application context
    single { BillingRepository(androidApplication()) }
}
