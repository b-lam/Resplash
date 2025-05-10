package com.b_lam.resplash.domain.autowallpaper

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.b_lam.resplash.data.autowallpaper.AutoWallpaperCollectionDao
import com.b_lam.resplash.data.autowallpaper.AutoWallpaperHistoryDao
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperHistory
import com.b_lam.resplash.data.collection.model.Collection
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoWallpaperRepository(
    private val autoWallpaperHistoryDao: AutoWallpaperHistoryDao,
    private val autoWallpaperCollectionDao: AutoWallpaperCollectionDao,
    private val fireStore: FirebaseFirestore = Firebase.firestore
) {

    fun getAutoWallpaperHistory(): LiveData<PagedList<AutoWallpaperHistory>> {
        return autoWallpaperHistoryDao.getAllAutoWallpaperHistory().toLiveData(
            Config(
                pageSize = 15,
                prefetchDistance = 5,
                initialLoadSizeHint = 15,
                enablePlaceholders = false
            )
        )
    }

    suspend fun addToAutoWallpaperHistory(wallpaper: AutoWallpaperHistory) =
        autoWallpaperHistoryDao.insert(wallpaper)

    suspend fun deleteAllAutoWallpaperHistory() = withContext(Dispatchers.IO) {
        autoWallpaperHistoryDao.deleteAllAutoWallpaperHistory()
    }

    suspend fun deleteOldAutoWallpaperHistory() = withContext(Dispatchers.IO) {
        autoWallpaperHistoryDao.deleteOldAutoWallpaperHistory()
    }

    fun getSelectedAutoWallpaperCollections(): LiveData<List<AutoWallpaperCollection>> {
        return autoWallpaperCollectionDao.getSelectedAutoWallpaperCollections()
    }

    fun getSelectedAutoWallpaperCollectionIds(): LiveData<List<String>> {
        return autoWallpaperCollectionDao.getSelectedAutoWallpaperCollectionIds()
    }

    suspend fun removeCollectionFromAutoWallpaper(collection: Collection) =
        removeCollectionFromAutoWallpaper(collection.id)

    suspend fun removeCollectionFromAutoWallpaper(id: String) = withContext(Dispatchers.IO) {
        autoWallpaperCollectionDao.delete(id)
    }

    suspend fun addCollectionToAutoWallpaper(collection: AutoWallpaperCollection) = withContext(Dispatchers.IO) {
        collection.date_added = System.currentTimeMillis()
        autoWallpaperCollectionDao.insert(collection)
    }

    suspend fun addCollectionToAutoWallpaper(collection: Collection) = withContext(Dispatchers.IO) {
        autoWallpaperCollectionDao.insert(
            AutoWallpaperCollection(
                collection.id,
                collection.title,
                collection.user?.name,
                collection.cover_photo?.urls?.regular,
                System.currentTimeMillis()
            )
        )
    }

    fun isCollectionUsedForAutoWallpaper(id: String) =
        autoWallpaperCollectionDao.getCountById(id).map { it > 0 }

    fun getNumberOfAutoWallpaperCollectionsLiveData() =
        autoWallpaperCollectionDao.getNumberOfAutoWallpaperCollectionsLiveData()

    suspend fun getRandomAutoWallpaperCollectionId(): String? {
        val count = autoWallpaperCollectionDao.getNumberOfAutoWallpaperCollections()
        return if (count > 0) {
            autoWallpaperCollectionDao.getRandomAutoWallpaperCollectionId((0 until count).random())
        } else {
            null
        }
    }

    fun getFeaturedCollections(): DocumentReference =
        fireStore.document("/autowallpaper/featured_v2")

    fun getPopularCollections(): DocumentReference =
        fireStore.document("/autowallpaper/popular_v2")
}