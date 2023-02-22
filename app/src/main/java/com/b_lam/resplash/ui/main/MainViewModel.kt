package com.b_lam.resplash.ui.main

import androidx.lifecycle.*
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.collection.CollectionPagingSource
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoPagingSource
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.launch

class MainViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository,
    billingRepository: BillingRepository
) : ViewModel() {

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

    private val _photoOrderLiveData = MutableLiveData(PhotoPagingSource.Companion.Order.LATEST)
    val photoOrderLiveData: LiveData<PhotoPagingSource.Companion.Order> = _photoOrderLiveData

    private val _collectionOrderLiveData = MutableLiveData(CollectionPagingSource.Companion.Order.ALL)
    val collectionOrderLiveData: LiveData<CollectionPagingSource.Companion.Order> = _collectionOrderLiveData

    private val photoListing: LiveData<Listing<Photo>> = Transformations.map(_photoOrderLiveData) {
        photoRepository.getPhotos(it)
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it.pagingData }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it.networkState }

    private val collectionListing = Transformations.map(_collectionOrderLiveData) {
        collectionRepository.getCollections(it)
    }
    val collectionsLiveData = Transformations.switchMap(collectionListing) { it.pagingData }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it.networkState }

    fun refreshPhotos() = photoListing.value?.refresh?.invoke()

    fun refreshCollections() = collectionListing.value?.refresh?.invoke()

    fun onNavigationItemSelected(optionId: Int) {
        _navigationItemSelectedLiveData.postValue(Event(optionId))
    }

    fun orderPhotosBy(selection: Int) {
        PhotoPagingSource.Companion.Order.values().getOrNull(selection)?.let {
            _photoOrderLiveData.postValue(it)
        }
    }

    fun orderCollectionsBy(selection: Int) {
        CollectionPagingSource.Companion.Order.values().getOrNull(selection)?.let {
            _collectionOrderLiveData.postValue(it)
        }
    }

    fun isUserLoggedIn() = loginRepository.isAuthorized()

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
}
