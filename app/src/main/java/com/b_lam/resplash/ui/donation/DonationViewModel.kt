package com.b_lam.resplash.ui.donation

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class DonationViewModel(
    private val billingRepository: BillingRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val productDetailsLiveData = billingRepository.productsWithProductDetails

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

    fun makePurchase(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchBillingFlow(activity, productDetails)
    }
}
