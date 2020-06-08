package com.b_lam.resplash.ui.photo

import android.content.Intent
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.util.downloadmanager.RxDownloadManager
import com.b_lam.resplash.util.fileName
import com.b_lam.resplash.util.getPhotoUrl
import org.koin.android.ext.android.inject

abstract class PhotoFragment : BaseSwipeRecyclerViewFragment<Photo>() {

    abstract override val preloadPagedListAdapter: PhotoAdapter

    private val downloadManager: RxDownloadManager by inject()

    val itemEventCallback = object : PhotoAdapter.ItemEventCallback {

        override fun onPhotoClick(photo: Photo) {
            Intent(context, PhotoDetailActivity::class.java).apply {
                putExtra(PhotoDetailActivity.EXTRA_PHOTO, photo)
                startActivity(this)
            }
        }

        override fun onUserClick(user: User) {
            Intent(context, UserActivity::class.java).apply {
                putExtra(UserActivity.EXTRA_USER, user)
                startActivity(this)
            }
        }

        override fun onLongClick(photo: Photo) {
            downloadManager.downloadPhoto(
                getPhotoUrl(photo, sharedPreferencesRepository.downloadQuality),
                photo.fileName
            )
        }
    }

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
