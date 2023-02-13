package com.b_lam.resplash.ui.upgrade

import android.app.Activity
import androidx.lifecycle.*
import com.b_lam.resplash.data.billing.Sku
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.combineWith
import kotlinx.coroutines.launch

class UpgradeViewModel(
    private val billingRepository: BillingRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val productDetailsLiveData = billingRepository.productsWithProductDetails

    val resplashProLiveData = billingRepository.resplashProLiveData

    val canPurchaseLiveData = productDetailsLiveData.combineWith(resplashProLiveData).map {
        val resplashProProductDetails = it.first?.get(Sku.RESPLASH_PRO)
        val entitled = it.second?.entitled
        return@map resplashProProductDetails != null && entitled != true
    }

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
        productDetailsLiveData.value?.get(Sku.RESPLASH_PRO)?.let {
            billingRepository.launchBillingFlow(activity, it)
        }
    }

    fun restorePurchase() = billingRepository.queryPurchasesAsync(restore = true)

    companion object {

        private const val RESPLASH_COLLECTION_ID = "10548247"
    }
}
