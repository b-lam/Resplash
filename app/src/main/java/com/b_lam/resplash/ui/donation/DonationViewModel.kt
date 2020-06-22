package com.b_lam.resplash.ui.donation

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.billing.model.AugmentedSkuDetails
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class DonationViewModel(
    private val billingRepository: BillingRepository,
    private val photoRepository: PhotoRepository
) : BaseViewModel() {

    init {
        billingRepository.startDataSourceConnections()
    }

    val skuDetailsLiveData = billingRepository.consumableSkuDetailsListLiveData

    val purchaseCompleteLiveData = billingRepository.purchaseCompleteLiveData

    private val _bannerPhotoLiveData by lazy {
        val liveData = MutableLiveData<Photo>()
        viewModelScope.launch {
            val result = photoRepository.getRandomPhoto(featured = true)
            if (result is Result.Success) liveData.value = result.value
        }
        return@lazy liveData
    }
    val bannerPhotoLiveData: LiveData<Photo> = _bannerPhotoLiveData

    fun makePurchase(activity: Activity, skuDetails: AugmentedSkuDetails) {
        billingRepository.launchBillingFlow(activity, skuDetails)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}