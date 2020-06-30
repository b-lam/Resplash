package com.b_lam.resplash.ui.photo

import android.Manifest
import android.animation.Animator
import android.content.Intent
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
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

        override fun onLongClick(photo: Photo, animationView: LottieAnimationView) {
            if (requireContext().fileExists(photo.fileName)) {
                showFileExistsDialog(requireContext()) { downloadPhoto(photo, animationView) }
            } else {
                downloadPhoto(photo, animationView)
            }
        }
    }

    private fun downloadPhoto(photo: Photo, animationView: LottieAnimationView) {
        if (requireContext().hasWritePermission()) {
            context.toast(R.string.download_started)
            animateLongClickDownload(animationView)
            DownloadJobIntentService.enqueueDownload(requireActivity().applicationContext,
                DownloadJobIntentService.Companion.Action.DOWNLOAD, photo.fileName,
                getPhotoUrl(photo, sharedPreferencesRepository.downloadQuality), photo.id)
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode = 0)
        }
    }

    private fun animateLongClickDownload(animationView: LottieAnimationView) {
        animationView.isVisible = true
        animationView.playAnimation()
        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                animationView.removeAnimatorListener(this)
                animationView.isVisible = false
            }
        })
    }

    override val emptyStateTitle: String
        get() = getString(R.string.empty_state_title)

    override val emptyStateSubtitle: String
        get() = ""

    override val itemSpacing: Int
        get() = resources.getDimensionPixelSize(R.dimen.keyline_7)
}
