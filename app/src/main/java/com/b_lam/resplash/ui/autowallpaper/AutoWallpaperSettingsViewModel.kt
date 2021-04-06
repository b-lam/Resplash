package com.b_lam.resplash.ui.autowallpaper

import androidx.lifecycle.ViewModel
import com.b_lam.resplash.domain.billing.BillingRepository

class AutoWallpaperSettingsViewModel(
    private val billingRepository: BillingRepository
) : ViewModel() {

    init {
        billingRepository.startDataSourceConnections()
    }

    val resplashProLiveData = billingRepository.resplashProLiveData

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}