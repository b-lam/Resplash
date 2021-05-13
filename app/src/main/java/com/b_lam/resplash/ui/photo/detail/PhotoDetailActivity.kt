package com.b_lam.resplash.ui.photo.detail

import android.Manifest
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.photo.model.Photo
import com.b_lam.resplash.databinding.ActivityPhotoDetailBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.collection.add.AddCollectionBottomSheet
import com.b_lam.resplash.ui.login.LoginActivity
import com.b_lam.resplash.ui.photo.zoom.PhotoZoomActivity
import com.b_lam.resplash.ui.search.SearchActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.download.*
import com.b_lam.resplash.worker.DownloadWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.Executors

class PhotoDetailActivity :
    BaseActivity(R.layout.activity_photo_detail), TagAdapter.ItemEventCallback {

    override val viewModel: PhotoDetailViewModel by viewModel()

    override val binding: ActivityPhotoDetailBinding by viewBinding()

    private lateinit var id: String

    private var downloadReceiver: BroadcastReceiver? = null

    private var snackbar: Snackbar? = null

    private val wallpaperManager: WallpaperManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        with(binding) {
            scrollView.doOnApplyWindowInsets { view, _, _ -> view.updatePadding(top = 0) }
            constraintLayout.doOnApplyWindowInsets { view, _, _ -> view.updatePadding(top = 0) }
            photoImageView.doOnApplyWindowInsets { view, _, _ -> view.updatePadding(top = 0) }
        }

        val photo = intent.getParcelableExtra<Photo>(EXTRA_PHOTO)
        val photoId = intent.getStringExtra(EXTRA_PHOTO_ID)

        when {
            photo != null -> id = photo.id
            photoId != null -> id = photoId
            else -> null
        }?.let {
            if (photo != null) setup(photo)
            viewModel.photoDetailsLiveData(id).observe(this) { photoDetails ->
                if (photo == null) {
                    setup(photoDetails)
                }
                displayPhotoDetails(photoDetails)
            }
        } ?: run {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        downloadReceiver = registerReceiver(IntentFilter(ACTION_DOWNLOAD_COMPLETE)) {
            it?.let { handleDownloadIntent(it) }
        }
    }

    override fun onStop() {
        super.onStop()

        downloadReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_photo_detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_show_description)?.isVisible =
            !viewModel.photoDetailsLiveData(id).value?.description.isNullOrBlank()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_description -> {
                showDescriptionDialog()
                true
            }
            R.id.action_open_in_browser -> {
                openPhotoInBrowser()
                true
            }
            R.id.action_share -> {
                sharePhoto()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setup(photo: Photo) {
        val url = getPhotoUrl(photo, sharedPreferencesRepository.loadQuality)
        binding.photoImageView.loadPhotoUrlWithThumbnail(
            url,
            photo.urls.thumb,
            photo.color,
            centerCrop = true
        )
        binding.photoImageView.setOnClickListener {
            Intent(this, PhotoZoomActivity::class.java).apply {
                putExtra(PhotoZoomActivity.EXTRA_PHOTO_URL, url)
                startActivity(this)
            }
        }
    }

    private fun displayPhotoDetails(photo: Photo) = with(binding) {
        contentLoadingLayout.hide()
        photo.user?.let { user ->
            userTextView.text = user.name ?: getString(R.string.unknown)
            userImageView.loadProfilePicture(user)
            userContainer.setOnClickListener {
                Intent(this@PhotoDetailActivity, UserActivity::class.java).apply {
                    putExtra(UserActivity.EXTRA_USER, user)
                    startActivity(this)
                }
            }
        }
        invalidateOptionsMenu()
        photo.location?.let { location ->
            val locationString = when {
                location.city != null && location.country != null ->
                    getString(R.string.location_template, location.city, location.country)
                location.city != null && location.country == null -> location.city
                location.city == null && location.country != null -> location.country
                else -> null
            }
            locationTextView.setTextAndVisibility(locationString)
            locationTextView.setOnClickListener { locationString?.let { openLocationInMaps(it) } }
        }
        exifRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = ExifAdapter(context).apply { setExif(photo) }
        }
        viewsCountTextView.text = (photo.views ?: 0).toPrettyString()
        downloadsCountTextView.text = (photo.downloads ?: 0).toPrettyString()
        likesCountTextView.text = (photo.likes ?: 0).toPrettyString()
        tagRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false).apply {
                addItemDecoration(
                    SpacingItemDecoration(
                        context,
                        R.dimen.keyline_6,
                        RecyclerView.HORIZONTAL
                    )
                )
            }
            adapter = TagAdapter(this@PhotoDetailActivity).apply { submitList(photo.tags) }
        }

        viewModel.currentUserCollectionIds.observe(this@PhotoDetailActivity) {
            setCollectButtonState(it ?: emptyList())
        }
        collectButton.setOnClickListener {
            if (viewModel.isUserAuthorized()) {
                AddCollectionBottomSheet
                    .newInstance(photo.id)
                    .show(supportFragmentManager, AddCollectionBottomSheet.TAG)
            } else {
                toast(R.string.need_to_log_in)
                startActivity(Intent(this@PhotoDetailActivity, LoginActivity::class.java))
            }
        }

        setLikeButtonState(photo.liked_by_user ?: false)
        likeButton.setOnClickListener {
            if (viewModel.isUserAuthorized()) {
                if (photo.liked_by_user == true) {
                    viewModel.unlikePhoto(photo.id)
                } else {
                    viewModel.likePhoto(photo.id)
                }
                photo.liked_by_user = photo.liked_by_user?.not()
                setLikeButtonState(photo.liked_by_user ?: false)
            } else {
                toast(R.string.need_to_log_in)
                startActivity(Intent(this@PhotoDetailActivity, LoginActivity::class.java))
            }
        }
        downloadButton.setOnClickListener {
            if (fileExists(photo.fileName, sharedPreferencesRepository.downloader)) {
                showFileExistsDialog(this@PhotoDetailActivity) { downloadPhoto(photo) }
            } else {
                downloadPhoto(photo)
            }
        }
        setAsWallpaperButton.setOnClickListener {
            if (fileExists(photo.fileName, sharedPreferencesRepository.downloader)) {
                getUriForPhoto(photo.fileName, sharedPreferencesRepository.downloader)?.let { uri ->
                    applyWallpaper(uri)
                } ?: run {
                    downloadWallpaper(photo)
                }
            } else {
                downloadWallpaper(photo)
            }
        }
        setAsWallpaperButton.show()
    }

    override fun onTagClick(tag: String) {
        Intent(this, SearchActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_SEARCH_QUERY, tag)
            startActivity(this)
        }
    }

    private fun setLikeButtonState(likedByUser: Boolean) {
        binding.likeButton.setImageResource(
            if (likedByUser) R.drawable.ic_favorite_filled_24dp
            else R.drawable.ic_favorite_border_24dp
        )
    }

    private fun setCollectButtonState(currentUserCollectionIds: List<String>) {
        binding.collectButton.setImageResource(
            if (currentUserCollectionIds.isNotEmpty()) R.drawable.ic_bookmark_filled_24dp
            else R.drawable.ic_bookmark_border_24dp
        )
    }

    private fun downloadPhoto(photo: Photo) {
        if (hasWritePermission()) {
            toast(R.string.download_started)
            val url = getPhotoUrl(photo, sharedPreferencesRepository.downloadQuality)
            if (sharedPreferencesRepository.downloader == DOWNLOADER_SYSTEM) {
                val downloadManagerWrapper: DownloadManagerWrapper by inject()
                viewModel.downloadId = downloadManagerWrapper.downloadPhoto(url, photo.fileName)
            } else {
                viewModel.downloadUUID = DownloadWorker.enqueueDownload(
                    applicationContext,
                    DownloadAction.DOWNLOAD,
                    url,
                    photo.fileName,
                    photo.id
                )
            }
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode = 0)
        }
    }

    private fun downloadWallpaper(photo: Photo) {
        if (hasWritePermission()) {
            val url = getPhotoUrl(photo, sharedPreferencesRepository.wallpaperQuality)
            if (sharedPreferencesRepository.downloader == DOWNLOADER_SYSTEM) {
                val downloadManagerWrapper: DownloadManagerWrapper by inject()
                viewModel.downloadId = downloadManagerWrapper.downloadWallpaper(url, photo.fileName)
            } else {
                viewModel.downloadUUID = DownloadWorker.enqueueDownload(
                    applicationContext,
                    DownloadAction.WALLPAPER,
                    url,
                    photo.fileName,
                    photo.id
                )
            }

            snackbar = Snackbar
                .make(
                    binding.coordinatorLayout,
                    R.string.setting_wallpaper,
                    Snackbar.LENGTH_INDEFINITE
                )
                .setAction(R.string.cancel) { cancelDownload() }
                .setActionTextColor(ContextCompat.getColor(this, R.color.red_400))
                .setAnchorView(R.id.set_as_wallpaper_button)
            snackbar?.show()
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, requestCode = 0)
        }
    }

    private fun handleDownloadIntent(intent: Intent) {
        val action = intent.getSerializableExtra(DATA_ACTION) as? DownloadAction
        val status = intent.getIntExtra(DOWNLOAD_STATUS, -1)

        if (action == DownloadAction.WALLPAPER) {
            snackbar?.dismiss()
            when (status) {
                STATUS_SUCCESSFUL -> intent.getParcelableExtra<Uri>(DATA_URI)?.let {
                    applyWallpaper(it)
                }
                STATUS_FAILED ->
                    binding.coordinatorLayout.showSnackBar(
                        R.string.oops,
                        anchor = R.id.set_as_wallpaper_button
                    )
            }
        } else if (action == DownloadAction.DOWNLOAD) {
            when (status) {
                STATUS_SUCCESSFUL -> toast(R.string.download_complete)
                STATUS_FAILED -> toast(R.string.oops)
            }
        }
    }

    private fun applyWallpaper(uri: Uri) {
        try {
            startActivity(wallpaperManager.getCropAndSetWallpaperIntent(uri))
        } catch (e: IllegalArgumentException) {
            viewModel.prepareBitmapFromUri(contentResolver, uri)
            observerWallpaperBitmapResult()
        }
    }

    private fun observerWallpaperBitmapResult() {
        viewModel.wallpaperBitmap.observe(this) { result ->
            if (result is Result.Success) {
                setWallpaperWithBitmap(result.value)
            } else {
                toast("Failed to set wallpaper")
            }
        }
    }

    private fun setWallpaperWithBitmap(bitmap: Bitmap) {
        lifecycleScope.launch {
            var isWallpaperSet = false
            withContext((Executors.newSingleThreadExecutor().asCoroutineDispatcher())) {
                try {
                    wallpaperManager.setBitmap(bitmap)
                    isWallpaperSet = true
                } catch (exception: IOException) {
                    error("Error setting wallpaper bitmap: $exception")
                }
            }
            withContext(Dispatchers.Main) {
                val resultMessage = if (isWallpaperSet) {
                    "Wallpaper set successfully"
                } else "Error setting wallpaper"
                this@PhotoDetailActivity.toast(resultMessage)
            }
        }
    }

    private fun cancelDownload() {
        if (sharedPreferencesRepository.downloader == DOWNLOADER_SYSTEM) {
            val downloadManagerWrapper: DownloadManagerWrapper by inject()
            viewModel.downloadId?.let { downloadManagerWrapper.cancelDownload(it) }
        } else {
            viewModel.downloadUUID?.let {
                WorkManager.getInstance(applicationContext).cancelWorkById(it)
            }
        }
    }

    private fun showDescriptionDialog() {
        val description = viewModel.photoDetailsLiveData(id).value?.description
        MaterialAlertDialogBuilder(this).setMessage(description).show()
    }

    private fun openPhotoInBrowser() {
        viewModel.photoDetailsLiveData(id).value?.links?.html?.let {
            CustomTabsHelper.openCustomTab(this, Uri.parse(it), sharedPreferencesRepository.theme)
        }
    }

    private fun sharePhoto() {
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, viewModel.photoDetailsLiveData(id).value?.links?.html)
            putExtra(Intent.EXTRA_TITLE, viewModel.photoDetailsLiveData(id).value?.description)
        }, null)
        startActivity(share)
    }

    companion object {

        const val EXTRA_PHOTO = "extra_photo"
        const val EXTRA_PHOTO_ID = "extra_photo_id"
    }
}
