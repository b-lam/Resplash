package com.b_lam.resplash.ui.user

import androidx.lifecycle.*
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.UserLikesDataSource
import com.b_lam.resplash.domain.photo.UserPhotoDataSource
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository
) : ViewModel() {

    private val _getUserResultLiveData = MutableLiveData<Event<Result<User>>>()
    val getUserResultLiveData: LiveData<Event<Result<User>>> = _getUserResultLiveData

    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> = _userLiveData

    private val photoListing = MutableLiveData<Listing<Photo>>()

    val photosLiveData = photoListing.switchMap { it.pagedList }
    val photosNetworkStateLiveData = photoListing.switchMap { it.networkState }
    val photosRefreshStateLiveData = photoListing.switchMap { it.refreshState }

    private val likesListing = MutableLiveData<Listing<Photo>>()

    val likesLiveData = likesListing.switchMap { it.pagedList }
    val likesNetworkStateLiveData = likesListing.switchMap { it.networkState }
    val likesRefreshStateLiveData = likesListing.switchMap { it.refreshState }

    private val collectionListing = MutableLiveData<Listing<Collection>>()

    val collectionsLiveData = collectionListing.switchMap { it.pagedList }
    val collectionsNetworkStateLiveData = collectionListing.switchMap { it.networkState }
    val collectionsRefreshStateLiveData = collectionListing.switchMap { it.refreshState }

    fun getUser(username: String) {
        viewModelScope.launch {
            val result = userRepository.getUserPublicProfile(username)
            if (result is Result.Success) {
                setUser(result.value)
            }
            _getUserResultLiveData.postValue(Event(result))
        }
    }

    fun setUser(user: User) = _userLiveData.postValue(user)

    fun getUserListings(username: String) {
        photoListing.postValue(
            photoRepository.getUserPhotos(
                username,
                UserPhotoDataSource.Companion.Order.LATEST,
                false,
                null,
                null,
                UserPhotoDataSource.Companion.Orientation.ALL,
                viewModelScope)
        )
        likesListing.postValue(
            photoRepository.getUserLikes(
                username,
                UserLikesDataSource.Companion.Order.LATEST,
                UserLikesDataSource.Companion.Orientation.ALL,
                viewModelScope
            )
        )
        collectionListing.postValue(
            collectionRepository.getUserCollections(
                username,
                viewModelScope
            )
        )
    }

    fun refreshPhotos() = photoListing.value?.refresh?.invoke()

    fun refreshLikes() = likesListing.value?.refresh?.invoke()

    fun refreshCollections() = collectionListing.value?.refresh?.invoke()

    fun isOwnProfile() = userLiveData.value?.username == loginRepository.getUsername()
}