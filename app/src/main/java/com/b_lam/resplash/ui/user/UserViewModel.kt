package com.b_lam.resplash.ui.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.UserLikesDataSourceFactory
import com.b_lam.resplash.domain.photo.UserPhotoDataSourceFactory
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository
) : BaseViewModel() {

    private val _userLiveData = MutableLiveData<User>()
    val userLiveData: LiveData<User> = _userLiveData

    private val photoListing = MutableLiveData<Listing<Photo>>()

    val photosLiveData = Transformations.switchMap(photoListing) { it.pagedList }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it.networkState }
    val photosRefreshStateLiveData = Transformations.switchMap(photoListing) { it.refreshState }

    private val likesListing = MutableLiveData<Listing<Photo>>()

    val likesLiveData = Transformations.switchMap(likesListing) { it.pagedList }
    val likesNetworkStateLiveData = Transformations.switchMap(likesListing) { it.networkState }
    val likesRefreshStateLiveData = Transformations.switchMap(likesListing) { it.refreshState }

    private val collectionListing = MutableLiveData<Listing<Collection>>()

    val collectionsLiveData = Transformations.switchMap(collectionListing) { it.pagedList }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it.networkState }
    val collectionsRefreshStateLiveData = Transformations.switchMap(collectionListing) { it.refreshState }

    fun getUser(username: String) {
        viewModelScope.launch {
            val result = userRepository.getUserPublicProfile(username)
            when (result) {
                is Result.Success -> setUser(result.value)
            }
        }
    }

    fun setUser(user: User) = _userLiveData.postValue(user)

    fun getUserListings(username: String) {
        photoListing.postValue(
            photoRepository.getUserPhotos(
                username,
                UserPhotoDataSourceFactory.Companion.Order.LATEST,
                false,
                null,
                null,
                UserPhotoDataSourceFactory.Companion.Orientation.ALL,
                viewModelScope)
        )
        likesListing.postValue(
            photoRepository.getUserLikes(
                username,
                UserLikesDataSourceFactory.Companion.Order.LATEST,
                UserLikesDataSourceFactory.Companion.Orientation.ALL,
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

}