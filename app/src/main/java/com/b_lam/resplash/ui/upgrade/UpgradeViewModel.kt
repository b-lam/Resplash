package com.b_lam.resplash.ui.upgrade

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class UpgradeViewModel(
    private val billingRepository: BillingRepository,
    private val photoRepository: PhotoRepository
) : BaseViewModel() {

    init {
        billingRepository.startDataSourceConnections()
    }

    val skuDetailsLiveData = billingRepository.resplashProSkuDetailsLiveData

    val resplashProLiveData = billingRepository.resplashProLiveData

    val billingMessageLiveData = billingRepository.billingMessageLiveData

    val billingErrorLiveData = billingRepository.billingErrorLiveData

    private val _bannerPhotoLiveData by lazy {
        val liveData = MutableLiveData<Photo>()
        viewModelScope.launch {
            val result = photoRepository.getRandomPhoto(collectionId = RESPLASH_COLLECTION_ID)
            if (result is Result.Success) liveData.value = result.value
        }
        return@lazy liveData
    }
    val bannerPhotoLiveData: LiveData<Photo> = _bannerPhotoLiveData

    fun makePurchase(activity: Activity) {
        skuDetailsLiveData.value?.let {
            billingRepository.launchBillingFlow(activity, it)
        }
    }

    fun restorePurchase() = billingRepository.queryPurchasesAsync()

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }

    companion object {

        private const val RESPLASH_COLLECTION_ID = 10548247
    }
}