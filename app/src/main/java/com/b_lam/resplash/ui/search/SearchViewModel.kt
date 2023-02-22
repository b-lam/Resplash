package com.b_lam.resplash.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.PagingNetworkState
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.SearchPhotoPagingSource
import com.b_lam.resplash.domain.user.UserRepository

class SearchViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _queryLiveData = MutableLiveData("")
    val queryLiveData: LiveData<String> = _queryLiveData

    private val _queryPhotoLiveData = MutableLiveData("")
    private val queryPhotoLiveData: LiveData<String> = _queryPhotoLiveData

    var order = SearchPhotoPagingSource.Companion.Order.RELEVANT
    var contentFilter = SearchPhotoPagingSource.Companion.ContentFilter.LOW
    var color = SearchPhotoPagingSource.Companion.Color.ANY
    var orientation = SearchPhotoPagingSource.Companion.Orientation.ANY

    private val photoListing: LiveData<Listing<Photo>?> = Transformations.map(queryPhotoLiveData) {
        if (it.isNotBlank()) {
            photoRepository.searchPhotos(
                it, order, null, contentFilter, color, orientation)
        } else {
            null
        }
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it?.pagingData }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) {
        it?.networkState ?: MutableLiveData(PagingNetworkState.Empty)
    }

    private val collectionListing: LiveData<Listing<Collection>?> = Transformations.map(queryLiveData) {
        if (it.isNotBlank()) {
            collectionRepository.searchCollections(it)
        } else {
            null
        }
    }
    val collectionsLiveData = Transformations.switchMap(collectionListing) { it?.pagingData }
    val collectionsNetworkStateLiveData = Transformations.switchMap(collectionListing) {
        it?.networkState ?: MutableLiveData(PagingNetworkState.Empty)
    }

    private val userListing: LiveData<Listing<User>?> = Transformations.map(queryLiveData) {
        if (it.isNotBlank()) {
            userRepository.searchUsers(it)
        } else {
            null
        }
    }
    val usersLiveData = Transformations.switchMap(userListing) { it?.pagingData }
    val usersNetworkStateLiveData = Transformations.switchMap(userListing) {
        it?.networkState ?: MutableLiveData(PagingNetworkState.Empty)
    }

    fun updateQuery(query: String) {
        _queryLiveData.postValue(query)
        _queryPhotoLiveData.postValue(query)
    }

    fun filterPhotoSearch() = _queryPhotoLiveData.postValue(queryPhotoLiveData.value)
}
