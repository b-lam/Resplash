package com.b_lam.resplash.ui.collection.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.databinding.ActivityCollectionDetailBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.user.UserCollectionFragment
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.CustomTabsHelper
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.livedata.observeOnce
import com.b_lam.resplash.worker.AutoWallpaperWorker
import com.google.android.apps.muzei.api.isSelected
import com.google.android.apps.muzei.api.provider.ProviderClient
import com.google.android.apps.muzei.api.provider.ProviderContract
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectionDetailActivity : BaseActivity(R.layout.activity_collection_detail) {

    override val viewModel: CollectionDetailViewModel by viewModel()

    override val binding: ActivityCollectionDetailBinding by viewBinding()

    private var providerClient: ProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val collection = intent.getParcelableExtra<Collection>(EXTRA_COLLECTION)
        val collectionId = intent.getStringExtra(EXTRA_COLLECTION_ID)

        when {
            collection != null -> viewModel.setCollection(collection)
            collectionId != null -> viewModel.getCollection(collectionId.toInt())
            else -> finish()
        }

        viewModel.collectionLiveData.observeOnce(this) { initialSetup(it) }
        viewModel.getCollectionResultLiveData.observeEvent(this) {
            if (it !is Result.Success) {
                toast(R.string.oops)
                finish()
            }
        }
        viewModel.updateCollectionResultLiveData.observe(this) {
            val result = it.peekContent()
            if (result is Result.Success) {
                setupToolbar(result.value)
                intent.apply {
                    putExtra(UserCollectionFragment.EXTRA_USER_COLLECTION_MODIFY_FLAG, true)
                    setResult(Activity.RESULT_OK, this)
                }
            }
        }
        viewModel.deleteCollectionResultLiveData.observe(this) {
            val result = it.peekContent()
            if (result is Result.Success) {
                intent.apply {
                    putExtra(UserCollectionFragment.EXTRA_USER_COLLECTION_DELETE_FLAG, true)
                    setResult(Activity.RESULT_OK, this)
                    finish()
                }
            }
        }

        providerClient = ProviderContract.getProviderClient(
            applicationContext, "${BuildConfig.APPLICATION_ID}.muzeiartprovider")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_collection_detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (isAutoWallpaperCollectionsEnabled()) {
            menu?.findItem(R.id.action_add_collection)?.isVisible =
                !viewModel.isCollectionUsedForAutoWallpaper
            menu?.findItem(R.id.action_remove_collection)?.isVisible =
                viewModel.isCollectionUsedForAutoWallpaper
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_in_browser -> {
                openCollectionInBrowser()
                true
            }
            R.id.action_share -> {
                shareCollection()
                true
            }
            R.id.action_add_collection -> {
                viewModel.addCollectionToAutoWallpaper()
                binding.rootContainer.showSnackBar(R.string.auto_wallpaper_collection_added)
                true
            }
            R.id.action_remove_collection -> {
                viewModel.removeCollectionFromAutoWallpaper()
                binding.rootContainer.showSnackBar(R.string.auto_wallpaper_collection_removed)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initialSetup(collection: Collection) = with(binding) {
        setupToolbar(collection)
        replaceFragmentInActivity(
            R.id.root_container,
            CollectionDetailFragment.newInstance(),
            CollectionDetailFragment::class.java.simpleName
        )
        toolbar.setOnClickListener {
            (supportFragmentManager.findFragmentByTag(CollectionDetailFragment::class.java.simpleName)
                    as? BaseSwipeRecyclerViewFragment<*, *>)?.scrollToTop()
            appBar.setExpanded(true)
        }

        contentLoadingLayout.hide()
        collectionContentLayout.isVisible = true

        collection.user?.let { user ->
            val count = resources.getQuantityString(
                R.plurals.photos, collection.total_photos, collection.total_photos)
            val name = getString(R.string.curated_by_template, user.name)
            userNameTextView.text = getString(R.string.bullet_template, count, name)
            userNameTextView.setOnClickListener {
                Intent(this@CollectionDetailActivity, UserActivity::class.java).apply {
                    putExtra(UserActivity.EXTRA_USER, user)
                    startActivity(this)
                }
            }
        }

        if (isAutoWallpaperCollectionsEnabled()) {
            viewModel.isCollectionUsedForAutoWallpaper(collection.id).observe(this@CollectionDetailActivity) {
                viewModel.isCollectionUsedForAutoWallpaper = it
                invalidateOptionsMenu()
            }
        }

        if (viewModel.isOwnCollection()) {
            editButton.show()
            editButton.setOnClickListener {
                EditCollectionBottomSheet
                    .newInstance()
                    .show(supportFragmentManager, EditCollectionBottomSheet.TAG)
            }
        }

        viewModel.getPhotoListing(collection.id)
    }

    private fun setupToolbar(collection: Collection) {
        setupActionBar(R.id.toolbar) {
            title = collection.title
            setDisplayHomeAsUpEnabled(true)
        }

        binding.descriptionTextView.setTextAndVisibility(collection.description?.trim())
    }

    private fun openCollectionInBrowser() {
        viewModel.collectionLiveData.value?.links?.html?.let {
            CustomTabsHelper.openCustomTab(this, Uri.parse(it), sharedPreferencesRepository.theme)
        }
    }

    private fun shareCollection() {
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, viewModel.collectionLiveData.value?.links?.html)
            putExtra(Intent.EXTRA_TITLE, viewModel.collectionLiveData.value?.title)
        }, null)
        startActivity(share)
    }

    private fun isAutoWallpaperCollectionsEnabled() =
        (sharedPreferencesRepository.autoWallpaperEnabled ||
                providerClient?.isSelected(applicationContext) ?: false) &&
                sharedPreferencesRepository.autoWallpaperSource ==
                AutoWallpaperWorker.Companion.Source.COLLECTIONS

    companion object {

        const val EXTRA_COLLECTION = "extra_collection"
        const val EXTRA_COLLECTION_ID = "extra_collection_id"
    }
}
