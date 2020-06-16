package com.b_lam.resplash.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.billing.BillingRepository
import com.b_lam.resplash.domain.collection.CollectionPagingSource
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoPagingSource
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.flow.Flow
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

    private val _photoOrderLiveData = MutableLiveData(PhotoPagingSource.Companion.Order.LATEST)
    val photoOrderLiveData: LiveData<PhotoPagingSource.Companion.Order> = _photoOrderLiveData

    private val _collectionOrderLiveData = MutableLiveData(CollectionPagingSource.Companion.Order.ALL)
    val collectionOrderLiveData: LiveData<CollectionPagingSource.Companion.Order> = _collectionOrderLiveData

    fun getPhotos(order: PhotoPagingSource.Companion.Order): Flow<PagingData<Photo>> {
        return photoRepository
            .getPhotos(order)
            .cachedIn(viewModelScope)
    }

    fun getCollections(order: CollectionPagingSource.Companion.Order): Flow<PagingData<Collection>> {
        return collectionRepository
            .getCollections(order)
            .cachedIn(viewModelScope)
    }

    fun onNavigationItemSelected(optionId: Int) {
        _navigationItemSelectedLiveData.postValue(Event(optionId))
    }

    fun orderPhotosBy(selection: Int) {
        val order = when (selection) {
            0 -> PhotoPagingSource.Companion.Order.LATEST
            1 -> PhotoPagingSource.Companion.Order.OLDEST
            else -> PhotoPagingSource.Companion.Order.POPULAR
        }
        _photoOrderLiveData.postValue(order)
    }

    fun orderCollectionsBy(selection: Int) {
        val order = when (selection) {
            0 -> CollectionPagingSource.Companion.Order.ALL
            else -> CollectionPagingSource.Companion.Order.FEATURED
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
