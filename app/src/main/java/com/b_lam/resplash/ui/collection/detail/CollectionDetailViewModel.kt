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
import com.b_lam.resplash.domain.photo.PhotoRepository
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.Result
import kotlinx.coroutines.launch

class CollectionDetailViewModel(
    private val photoRepository: PhotoRepository,
    private val collectionRepository: CollectionRepository,
    private val autoWallpaperRepository: AutoWallpaperRepository
) : BaseViewModel() {

    private val collectionMutableLiveData = MutableLiveData<Collection>()
    val collectionLiveData: LiveData<Collection> = collectionMutableLiveData

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
            when (result) {
                is Result.Success -> setCollection(result.value)
            }
        }
    }

    fun setCollection(collection: Collection) = collectionMutableLiveData.postValue(collection)

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
}