package com.b_lam.resplash.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.collection.CollectionDataSourceFactory
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoDataSourceFactory
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.launch

class MainViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository,
    private val billingRepository: BillingRepository
) : BaseViewModel() {

    init {
        billingRepository.startDataSourceConnections()
    }

    private val _navigationItemSelectedLiveData = MutableLiveData<Event<Int>>()
    val navigationItemSelectedLiveData: LiveData<Event<Int>> = _navigationItemSelectedLiveData

    private val _authorizedLiveData = MutableLiveData(loginRepository.isAuthorized())
    val authorizedLiveData: LiveData<Boolean> = _authorizedLiveData

    val resplashProLiveData = billingRepository.resplashProLiveData

    private val _usernameLiveData = MutableLiveData<String?>()
    val usernameLiveData: LiveData<String?> = _usernameLiveData

    private val _emailLiveData = MutableLiveData<String?>()
    val emailLiveData: LiveData<String?> = _emailLiveData

    private val _profilePictureLiveData = MutableLiveData<String?>()
    val profilePictureLiveData: LiveData<String?> = _profilePictureLiveData

    private val _photoOrderLiveData = MutableLiveData(PhotoDataSourceFactory.Companion.Order.LATEST)
    val photoOrderLiveData: LiveData<PhotoDataSourceFactory.Companion.Order> = _photoOrderLiveData

    private val _collectionOrderLiveData = MutableLiveData(CollectionDataSourceFactory.Companion.Order.ALL)
    val collectionOrderLiveData: LiveData<CollectionDataSourceFactory.Companion.Order> = _collectionOrderLiveData

    private val photoListing: LiveData<Listing<Photo>> = Transformations.map(_photoOrderLiveData) {
        photoRepository.getPhotos(it, viewModelScope)
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it.pagedList }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it.networkState }
    val photosRefreshStateLiveData = Transformations.switchMap(photoListing) { it.refreshState }

    private val collectionListing = Transformations.map(_collectionOrderLiveData) {
        collectionRepository.getCollections(it, viewModelScope)
    }
    val collectionsLiveData = Transformations.switchMap(collectionListing) { it.pagedList }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it.networkState }
    val collectionsRefreshStateLiveData = Transformations.switchMap(collectionListing) { it.refreshState }

    fun refreshPhotos() = photoListing.value?.refresh?.invoke()

    fun refreshCollections() = collectionListing.value?.refresh?.invoke()

    fun onNavigationItemSelected(optionId: Int) {
        _navigationItemSelectedLiveData.postValue(Event(optionId))
    }

    fun orderPhotosBy(selection: Int) {
        val order = when (selection) {
            0 -> PhotoDataSourceFactory.Companion.Order.LATEST
            1 -> PhotoDataSourceFactory.Companion.Order.OLDEST
            else -> PhotoDataSourceFactory.Companion.Order.POPULAR
        }
        _photoOrderLiveData.postValue(order)
    }

    fun orderCollectionsBy(selection: Int) {
        val order = when (selection) {
            0 -> CollectionDataSourceFactory.Companion.Order.ALL
            else -> CollectionDataSourceFactory.Companion.Order.FEATURED
        }
        _collectionOrderLiveData.postValue(order)
    }

    fun refreshUserProfile() {
        if (loginRepository.isAuthorized()) {
            _authorizedLiveData.postValue(true)
            updateUser(loginRepository.getUsername(), loginRepository.getEmail(), loginRepository.getProfilePicture())
            if (loginRepository.getUsername().isNullOrBlank()) {
                viewModelScope.launch {
                    val result = loginRepository.getMe()
                    if (result is Result.Success) {
                        val me = result.value
                        updateUser(me.username, me.email, me.profile_image?.large)
                    }
                }
            }
        }
    }

    fun trackDownload(id: String) = viewModelScope.launch { photoRepository.trackDownload(id) }

    fun logout() {
        loginRepository.logout()
        _authorizedLiveData.postValue(false)
        updateUser(null, null, null)
    }

    private fun updateUser(username: String?, email: String?, profilePicture: String?) {
        _usernameLiveData.postValue(username)
        _emailLiveData.postValue(email)
        _profilePictureLiveData.postValue(profilePicture)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}
