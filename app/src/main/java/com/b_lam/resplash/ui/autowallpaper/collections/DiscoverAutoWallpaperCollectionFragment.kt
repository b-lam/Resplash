package com.b_lam.resplash.ui.autowallpaper.collections

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.ui.base.BaseFragment
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.showSnackBar
import cz.intik.overflowindicator.SimpleSnapHelper
import kotlinx.android.synthetic.main.fragment_discover_auto_wallpaper_collection.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DiscoverAutoWallpaperCollectionFragment :
    BaseFragment(), AutoWallpaperCollectionListAdapter.ItemEventCallback {

    override val layoutId = R.layout.fragment_discover_auto_wallpaper_collection

    private val sharedViewModel: AutoWallpaperCollectionViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val featuredCollectionsAdapter =
            AutoWallpaperCollectionListAdapter(AutoWallpaperCollectionListAdapter.ItemType.FEATURED, this)

        featured_collection_recycler_view.apply {
            adapter = featuredCollectionsAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            page_indicator.attachToRecyclerView(this)
            SimpleSnapHelper(page_indicator).attachToRecyclerView(this)
        }

        val popularCollectionsAdapter =
            AutoWallpaperCollectionListAdapter(AutoWallpaperCollectionListAdapter.ItemType.POPULAR, this)

        popular_collections_recycler_view.apply {
            adapter = popularCollectionsAdapter
            layoutManager = LinearLayoutManager(context).apply {
                addItemDecoration(SpacingItemDecoration(context, R.dimen.keyline_7))
            }
        }

        with(sharedViewModel) {
            featuredCollectionLiveData.observe(viewLifecycleOwner) {
                featuredCollectionsAdapter.submitList(it)
            }
            popularCollectionLiveData.observe(viewLifecycleOwner) {
                popular_title_text_view.isVisible = it.isNotEmpty()
                popularCollectionsAdapter.submitList(it)
            }
            selectedAutoWallpaperCollectionIds.observe(viewLifecycleOwner) {
                featuredCollectionsAdapter.setSelectedCollectionIds(it)
                popularCollectionsAdapter.setSelectedCollectionIds(it)
            }
        }
    }

    override fun onCollectionClick(id: Int) {
        Intent(context, CollectionDetailActivity::class.java).apply {
            putExtra(CollectionDetailActivity.EXTRA_COLLECTION_ID, id.toString())
            startActivity(this)
        }
    }

    override fun onAddClick(collection: AutoWallpaperCollection) {
        sharedViewModel.addAutoWallpaperCollection(collection)
        root_container.showSnackBar(R.string.auto_wallpaper_collection_added)
    }

    override fun onRemoveClick(id: Int) {
        sharedViewModel.removeAutoWallpaperCollection(id)
        root_container.showSnackBar(R.string.auto_wallpaper_collection_removed)
    }

    companion object {

        fun newInstance() = DiscoverAutoWallpaperCollectionFragment()
    }
}
