package com.b_lam.resplash.ui.autowallpaper

import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.ui.base.BaseViewModel

class AutoWallpaperSettingsViewModel(
    private val billingRepository: BillingRepository
) : BaseViewModel() {

    init {
        billingRepository.startDataSourceConnections()
    }

    val resplashProLiveData = billingRepository.resplashProLiveData

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}