package com.b_lam.resplash.ui.search

import androidx.lifecycle.*
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.SearchPhotoDataSource
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.util.NetworkState

class SearchViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _queryLiveData = MutableLiveData("")
    val queryLiveData: LiveData<String> = _queryLiveData

    private val _queryPhotoLiveData = MutableLiveData("")
    private val queryPhotoLiveData: LiveData<String> = _queryPhotoLiveData

    var order = SearchPhotoDataSource.Companion.Order.RELEVANT
    var contentFilter = SearchPhotoDataSource.Companion.ContentFilter.LOW
    var color = SearchPhotoDataSource.Companion.Color.ANY
    var orientation = SearchPhotoDataSource.Companion.Orientation.ANY

    private val photoListing: LiveData<Listing<Photo>?> = Transformations.map(queryPhotoLiveData) {
        if (it.isNotBlank()) {
            photoRepository.searchPhotos(
                it, order, null, contentFilter, color, orientation, viewModelScope)
        } else {
            null
        }
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it?.pagedList }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it?.networkState }
    val photosRefreshStateLiveData = Transformations.switchMap(photoListing) {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    private val collectionListing: LiveData<Listing<Collection>?> = Transformations.map(queryLiveData) {
        if (it.isNotBlank()) {
            collectionRepository.searchCollections(it, viewModelScope)
        } else {
            null
        }
    }
    val collectionsLiveData = Transformations.switchMap(collectionListing) { it?.pagedList }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) { it?.networkState }
    val collectionsRefreshStateLiveData = Transformations.switchMap(collectionListing) {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    private val userListing: LiveData<Listing<User>?> = Transformations.map(queryLiveData) {
        if (it.isNotBlank()) {
            userRepository.searchUsers(it, viewModelScope)
        } else {
            null
        }
    }
    val usersLiveData = Transformations.switchMap(userListing) { it?.pagedList }
    val usersNetworkStateLiveData = Transformations.switchMap(userListing) { it?.networkState }
    val usersRefreshStateLiveData = Transformations.switchMap(userListing) {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    fun updateQuery(query: String) {
        _queryLiveData.postValue(query)
        _queryPhotoLiveData.postValue(query)
    }

    fun filterPhotoSearch() = _queryPhotoLiveData.postValue(queryPhotoLiveData.value)
}