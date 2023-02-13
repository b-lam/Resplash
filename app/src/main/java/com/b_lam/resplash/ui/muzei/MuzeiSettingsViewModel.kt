package com.b_lam.resplash.ui.muzei

import androidx.lifecycle.ViewModel
import com.b_lam.resplash.domain.billing.BillingRepository

class MuzeiSettingsViewModel(
    billingRepository: BillingRepository
) : ViewModel() {

    val resplashProLiveData = billingRepository.resplashProLiveData
}
