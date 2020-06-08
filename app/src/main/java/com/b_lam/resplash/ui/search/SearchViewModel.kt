package com.b_lam.resplash.ui.search

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
import com.b_lam.resplash.domain.photo.SearchPhotoDataSourceFactory
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.NetworkState

class SearchViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _queryLiveData = MutableLiveData("")
    val queryLiveData: LiveData<String> = _queryLiveData

    private val _queryPhotoLiveData = MutableLiveData("")
    private val queryPhotoLiveData: LiveData<String> = _queryPhotoLiveData

    private val _orderLiveData = MutableLiveData(SearchPhotoDataSourceFactory.Companion.Order.RELEVANT)
    val orderLiveData: LiveData<SearchPhotoDataSourceFactory.Companion.Order> = _orderLiveData

    private val _contentFilterLiveData = MutableLiveData(SearchPhotoDataSourceFactory.Companion.ContentFilter.LOW)
    val contentFilterLiveData: LiveData<SearchPhotoDataSourceFactory.Companion.ContentFilter> = _contentFilterLiveData

    private val _colorLiveData = MutableLiveData(SearchPhotoDataSourceFactory.Companion.Color.ANY)
    val colorLiveData: LiveData<SearchPhotoDataSourceFactory.Companion.Color> = _colorLiveData

    private val _orientationLiveData = MutableLiveData(SearchPhotoDataSourceFactory.Companion.Orientation.ANY)
    val orientationLiveData: LiveData<SearchPhotoDataSourceFactory.Companion.Orientation> = _orientationLiveData

    private val photoListing: LiveData<Listing<Photo>?> = Transformations.map(queryPhotoLiveData) {
        if (it.isNotBlank()) {
            photoRepository.searchPhotos(
                it,
                orderLiveData.value,
                null,
                contentFilterLiveData.value,
                colorLiveData.value,
                orientationLiveData.value,
                viewModelScope
            )
        } else {
            null
        }
    }
    val photosLiveData = Transformations.switchMap(photoListing) { it?.pagedList }
    val photosNetworkStateLiveData = Transformations.switchMap(photoListing) { it?.networkState }
    val photosRefreshStateLiveData = Transformations.switchMap(photoListing) {
        it?.refreshState ?: MutableLiveData<NetworkState>(NetworkState.EMPTY)
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
        it?.refreshState ?: MutableLiveData<NetworkState>(NetworkState.EMPTY)
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
        it?.refreshState ?: MutableLiveData<NetworkState>(NetworkState.EMPTY)
    }

    fun updateQuery(newQuery: String) {
        _queryLiveData.postValue(newQuery)
        _queryPhotoLiveData.postValue(newQuery)
    }

    fun updateOrder(order: SearchPhotoDataSourceFactory.Companion.Order) {
        _orderLiveData.postValue(order)
    }

    fun updateContentFilter(contentFilter: SearchPhotoDataSourceFactory.Companion.ContentFilter) {
        _contentFilterLiveData.postValue(contentFilter)
    }

    fun updateColor(color: SearchPhotoDataSourceFactory.Companion.Color) {
        _colorLiveData.postValue(color)
    }

    fun updateOrientation(orientation: SearchPhotoDataSourceFactory.Companion.Orientation) {
        _orientationLiveData.postValue(orientation)
    }

    fun updatePhotoSearch() {
        _queryPhotoLiveData.postValue(queryPhotoLiveData.value)
    }
}