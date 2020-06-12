package com.b_lam.resplash.ui.collection.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.domain.Listing
import com.b_lam.resplash.domain.autowallpaper.AutoWallpaperRepository
import com.b_lam.resplash.domain.collection.CollectionRepository
import com.b_lam.resplash.domain.login.LoginRepository
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.livedata.Event
import kotlinx.coroutines.launch
import retrofit2.Response

class CollectionDetailViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val loginRepository: LoginRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : BaseViewModel() {

    private val _getCollectionResultLiveData = MutableLiveData<Event<Result<Collection>>>()
    val getCollectionResultLiveData: LiveData<Event<Result<Collection>>> = _getCollectionResultLiveData

    private val _updateCollectionResultLiveData = MutableLiveData<Event<Result<Collection>>>()
    val updateCollectionResultLiveData: LiveData<Event<Result<Collection>>> = _updateCollectionResultLiveData

    private val _deleteCollectionResultLiveData = MutableLiveData<Event<Result<Response<Unit>>>>()
    val deleteCollectionResultLiveData: LiveData<Event<Result<Response<Unit>>>> = _deleteCollectionResultLiveData

    private val _collectionLiveData = MutableLiveData<Collection>()
    val collectionLiveData: LiveData<Collection> = _collectionLiveData

    private val photoListing = MutableLiveData<Listing<Photo>>()

    val photosLiveData = Transformations.switchMap(photoListing) { it.pagedList }
    val networkStateLiveData = Transformations.switchMap(photoListing) { it.networkState }
    val refreshStateLiveData = Transformations.switchMap(photoListing) { it.refreshState }

    var isCollectionUsedForAutoWallpaper = false

    fun getPhotoListing(collectionId: Int) {
        photoListing.postValue(photoRepository.getCollectionPhotos(collectionId, viewModelScope))
    }

    fun getCollection(collectionId: Int) {
        viewModelScope.launch {
            val result = collectionRepository.getCollection(collectionId)
            if (result is Result.Success) {
                setCollection(result.value)
            }
            _getCollectionResultLiveData.postValue(Event(result))
        }
    }

    fun setCollection(collection: Collection) = _collectionLiveData.postValue(collection)

    fun isOwnCollection() = collectionLiveData.value?.user?.username == loginRepository.getUsername()

    fun isCollectionUsedForAutoWallpaper(id: Int) =
        autoWallpaperRepository.isCollectionUsedForAutoWallpaper(id)

    fun addCollectionToAutoWallpaper() {
        viewModelScope.launch {
            collectionLiveData.value?.let { autoWallpaperRepository.addCollectionToAutoWallpaper(it) }
        }
    }

    fun removeCollectionFromAutoWallpaper() {
        viewModelScope.launch {
            collectionLiveData.value?.let { autoWallpaperRepository.removeCollectionFromAutoWallpaper(it) }
        }
    }

    fun updateCollection(
        title: String,
        description: String?,
        private: Boolean?
    ) {
        collectionLiveData.value?.id?.let {
            viewModelScope.launch {
                val result = collectionRepository.updateCollection(it, title, description, private)
                if (result is Result.Success) {
                    _collectionLiveData.postValue(result.value)
                }
                _updateCollectionResultLiveData.postValue(Event(result))
            }
        }
    }

    fun deleteCollection() {
        collectionLiveData.value?.id?.let {
            viewModelScope.launch {
                val result = collectionRepository.deleteCollection(it)
                _deleteCollectionResultLiveData.postValue(Event(result))
            }
        }
    }

    fun trackDownload(id: String) = viewModelScope.launch { photoRepository.trackDownload(id) }
}