package com.b_lam.resplash.ui.photo

import android.Manifest
import android.content.Intent
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.service.DownloadJobIntentService
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.util.*

abstract class PhotoFragment : BaseSwipeRecyclerViewFragment<Photo>() {

    abstract override val pagedListAdapter: PhotoAdapter

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
            if (requireContext().hasWritePermission()) {
                context.toast(R.string.download_started)
                DownloadJobIntentService.enqueueDownload(requireContext(),
                    DownloadJobIntentService.Companion.Action.DOWNLOAD, photo.fileName,
                    getPhotoUrl(photo, sharedPreferencesRepository.downloadQuality), photo.id)
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode = 0)
            }
        }
    }

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
