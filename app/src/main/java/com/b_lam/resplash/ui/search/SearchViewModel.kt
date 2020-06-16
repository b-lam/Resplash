package com.b_lam.resplash.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.domain.photo.SearchPhotoPagingSource
import com.b_lam.resplash.domain.user.UserRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SearchViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private val _queryLiveData = MutableLiveData("")
    val queryLiveData: LiveData<String> = _queryLiveData

    private val _queryPhotoLiveData = MutableLiveData("")
    val queryPhotoLiveData: LiveData<String> = _queryPhotoLiveData

    var order = SearchPhotoPagingSource.Companion.Order.RELEVANT
    var contentFilter = SearchPhotoPagingSource.Companion.ContentFilter.LOW
    var color = SearchPhotoPagingSource.Companion.Color.ANY
    var orientation = SearchPhotoPagingSource.Companion.Orientation.ANY

    fun searchPhotos(query: String): Flow<PagingData<Photo>> {
        return if (query.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            photoRepository.searchPhotos(
                query, order, null, contentFilter, color, orientation
            ).cachedIn(viewModelScope)
        }
    }

    fun searchCollections(query: String): Flow<PagingData<Collection>> {
        return if (query.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            collectionRepository.searchCollections(query).cachedIn(viewModelScope)
        }
    }

    fun searchUsers(query: String): Flow<PagingData<User>> {
        return if (query.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            userRepository.searchUsers(query).cachedIn(viewModelScope)
        }
    }

    fun updateQuery(query: String) {
        _queryLiveData.postValue(query)
        _queryPhotoLiveData.postValue(query)
    }

    fun filterPhotoSearch() = _queryPhotoLiveData.postValue(queryPhotoLiveData.value)

    fun trackDownload(id: String) = viewModelScope.launch { photoRepository.trackDownload(id) }
}