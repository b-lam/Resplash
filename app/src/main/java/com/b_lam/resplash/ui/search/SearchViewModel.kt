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

    private val photoListing: LiveData<Listing<Photo>?> = queryPhotoLiveData.map {
        if (it.isNotBlank()) {
            photoRepository.searchPhotos(
                it, order, null, contentFilter, color, orientation, viewModelScope)
        } else {
            null
        }
    }
    val photosLiveData = photoListing.switchMap { it?.pagedList }
    val photosNetworkStateLiveData = photoListing.switchMap { it?.networkState }
    val photosRefreshStateLiveData = photoListing.switchMap {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    private val collectionListing: LiveData<Listing<Collection>?> = queryLiveData.map {
        if (it.isNotBlank()) {
            collectionRepository.searchCollections(it, viewModelScope)
        } else {
            null
        }
    }
    val collectionsLiveData = collectionListing.switchMap { it?.pagedList }
    val collectionsNetworkStateLiveData = collectionListing.switchMap { it?.networkState }
    val collectionsRefreshStateLiveData = collectionListing.switchMap {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    private val userListing: LiveData<Listing<User>?> = queryLiveData.map {
        if (it.isNotBlank()) {
            userRepository.searchUsers(it, viewModelScope)
        } else {
            null
        }
    }
    val usersLiveData = userListing.switchMap { it?.pagedList }
    val usersNetworkStateLiveData = userListing.switchMap { it?.networkState }
    val usersRefreshStateLiveData = userListing.switchMap {
        it?.refreshState ?: MutableLiveData(NetworkState.EMPTY)
    }

    fun updateQuery(query: String) {
        _queryLiveData.postValue(query)
        _queryPhotoLiveData.postValue(query)
    }

    fun filterPhotoSearch() = _queryPhotoLiveData.postValue(queryPhotoLiveData.value)
}
