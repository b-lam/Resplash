package com.b_lam.resplash.ui.photo

import android.Manifest
import android.content.Intent
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.photo.detail.PhotoDetailActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.util.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.android.ext.android.inject

abstract class PhotoFragment : BaseSwipeRecyclerViewFragment<Photo>() {

    abstract override val pagedListAdapter: PhotoAdapter

    private val downloadManager: RxDownloadManager by inject()

    private val compositeDisposable = CompositeDisposable()

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
                compositeDisposable += downloadManager.downloadPhoto(
                    getPhotoUrl(photo, sharedPreferencesRepository.downloadQuality),
                    photo.fileName
                ).second.doOnSubscribe {
                    context.toast(R.string.download_started)
                }.doAfterTerminate {
                    compositeDisposable.clear()
                }.subscribeBy(
                    onNext = { trackDownload(photo.id) },
                    onError = {}
                )
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode = 0)
            }
        }
    }

    abstract fun trackDownload(id: String)

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
