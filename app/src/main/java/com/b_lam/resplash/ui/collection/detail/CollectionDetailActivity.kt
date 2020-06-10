package com.b_lam.resplash.ui.collection.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.data.collection.model.Collection
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.user.UserCollectionFragment
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.customtabs.CustomTabsHelper
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.livedata.observeOnce
import com.b_lam.resplash.worker.AutoWallpaperWorker
import kotlinx.android.synthetic.main.activity_collection_detail.*
import kotlinx.android.synthetic.main.activity_user.user_name_text_view
import org.koin.androidx.viewmodel.ext.android.viewModel

class CollectionDetailActivity : BaseActivity() {

    override val viewModel: CollectionDetailViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_detail)

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
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_collection_detail, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (sharedPreferencesRepository.autoWallpaperEnabled &&
            sharedPreferencesRepository.autoWallpaperSource ==
            AutoWallpaperWorker.Companion.Source.COLLECTIONS) {
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
                root_container.showSnackBar(R.string.auto_wallpaper_collection_added)
                true
            }
            R.id.action_remove_collection -> {
                viewModel.removeCollectionFromAutoWallpaper()
                root_container.showSnackBar(R.string.auto_wallpaper_collection_removed)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initialSetup(collection: Collection) {
        setupToolbar(collection)
        replaceFragmentInActivity(
            R.id.root_container,
            CollectionDetailFragment.newInstance(),
            CollectionDetailFragment::class.java.simpleName
        )
        toolbar.setOnClickListener {
            (supportFragmentManager.findFragmentByTag(CollectionDetailFragment::class.java.simpleName)
                    as? BaseSwipeRecyclerViewFragment<*>)?.scrollToTop()
            app_bar.setExpanded(true)
        }

        content_loading_layout.hide()
        collection_content_layout.isVisible = true

        collection.user?.let { user ->
            val count = resources.getQuantityString(
                R.plurals.photos, collection.total_photos, collection.total_photos)
            val name = getString(R.string.curated_by_template, user.name)
            user_name_text_view.text = getString(R.string.bullet_template, count, name)
            user_name_text_view.setOnClickListener {
                Intent(this, UserActivity::class.java).apply {
                    putExtra(UserActivity.EXTRA_USER, user)
                    startActivity(this)
                }
            }
        }

        if (sharedPreferencesRepository.autoWallpaperEnabled &&
            sharedPreferencesRepository.autoWallpaperSource ==
            AutoWallpaperWorker.Companion.Source.COLLECTIONS) {
            viewModel.isCollectionUsedForAutoWallpaper(collection.id).observe(this) {
                viewModel.isCollectionUsedForAutoWallpaper = it
                invalidateOptionsMenu()
            }
        }

        if (viewModel.isOwnCollection()) {
            edit_button.show()
            edit_button.setOnClickListener {
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

        description_text_view.setTextOrHide(collection.description?.trim())
    }

    private fun openCollectionInBrowser() {
        val uri = Uri.parse(viewModel.collectionLiveData.value?.links?.html)
        CustomTabsHelper.openCustomTab(this, uri, sharedPreferencesRepository.theme)
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

    companion object {

        const val EXTRA_COLLECTION = "extra_collection"
        const val EXTRA_COLLECTION_ID = "extra_collection_id"
    }
}
