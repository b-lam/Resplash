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

    private val navigationItemSelectedMutableLiveData = MutableLiveData<Event<Int>>()
    val navigationItemSelectedLiveData: LiveData<Event<Int>>
        get() = navigationItemSelectedMutableLiveData

    private val authorizedMutableLiveData = MutableLiveData(loginRepository.isAuthorized())
    val authorizedLiveData: LiveData<Boolean>
        get() = authorizedMutableLiveData

    val resplashProLiveData = billingRepository.resplashProLiveData

    private val usernameMutableLiveData = MutableLiveData<String?>()
    val usernameLiveData: LiveData<String?>
        get() = usernameMutableLiveData

    private val emailMutableLiveData = MutableLiveData<String?>()
    val emailLiveData: LiveData<String?>
        get() = emailMutableLiveData

    private val profilePictureMutableLiveData = MutableLiveData<String?>()
    val profilePictureLiveData: LiveData<String?>
        get() = profilePictureMutableLiveData

    private val photoOrderMutableLiveData = MutableLiveData(PhotoDataSourceFactory.Companion.Order.LATEST)
    val photoOrderLiveData: LiveData<PhotoDataSourceFactory.Companion.Order>
        get() = photoOrderMutableLiveData

    private val collectionOrderMutableLiveData = MutableLiveData(CollectionDataSourceFactory.Companion.Order.ALL)
    val collectionOrderLiveData: LiveData<CollectionDataSourceFactory.Companion.Order>
        get() = collectionOrderMutableLiveData

    private val photoListing: LiveData<Listing<Photo>> = Transformations.map(photoOrderMutableLiveData) {
        photoRepository.getPhotos(it, viewModelScope)
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it.pagedList }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it.networkState }
    val photosRefreshStateLiveData = Transformations.switchMap(photoListing) { it.refreshState }

    private val collectionListing = Transformations.map(collectionOrderMutableLiveData) {
        collectionRepository.getCollections(it, viewModelScope)
    }
    val collectionsLiveData = Transformations.switchMap(collectionListing) { it.pagedList }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it.networkState }
    val collectionsRefreshStateLiveData = Transformations.switchMap(collectionListing) { it.refreshState }

    fun refreshPhotos() = photoListing.value?.refresh?.invoke()

    fun refreshCollections() = collectionListing.value?.refresh?.invoke()

    fun onNavigationItemSelected(optionId: Int) {
        navigationItemSelectedMutableLiveData.postValue(Event(optionId))
    }

    fun orderPhotosBy(selection: Int) {
        val order = when (selection) {
            0 -> PhotoDataSourceFactory.Companion.Order.LATEST
            1 -> PhotoDataSourceFactory.Companion.Order.OLDEST
            else -> PhotoDataSourceFactory.Companion.Order.POPULAR
        }
        photoOrderMutableLiveData.postValue(order)
    }

    fun orderCollectionsBy(selection: Int) {
        val order = when (selection) {
            0 -> CollectionDataSourceFactory.Companion.Order.ALL
            else -> CollectionDataSourceFactory.Companion.Order.FEATURED
        }
        collectionOrderMutableLiveData.postValue(order)
    }

    fun onLikeClick(photo: Photo) {
        if (photo.liked_by_user == true) {
            viewModelScope.launch { photoRepository.unlikePhoto(photo.id) }
        } else {
            viewModelScope.launch { photoRepository.likePhoto(photo.id) }
        }
    }

    fun refreshUserProfile() {
        if (loginRepository.isAuthorized()) {
            authorizedMutableLiveData.postValue(true)
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
        authorizedMutableLiveData.postValue(false)
        updateUser(null, null, null)
    }

    private fun updateUser(username: String?, email: String?, profilePicture: String?) {
        usernameMutableLiveData.postValue(username)
        emailMutableLiveData.postValue(email)
        profilePictureMutableLiveData.postValue(profilePicture)
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.endDataSourceConnections()
    }
}
