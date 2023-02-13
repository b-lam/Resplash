package com.b_lam.resplash.ui.autowallpaper

import androidx.lifecycle.ViewModel
import com.b_lam.resplash.domain.billing.BillingRepository

class AutoWallpaperSettingsViewModel(
    billingRepository: BillingRepository
) : ViewModel() {

    val resplashProLiveData = billingRepository.resplashProLiveData
}
