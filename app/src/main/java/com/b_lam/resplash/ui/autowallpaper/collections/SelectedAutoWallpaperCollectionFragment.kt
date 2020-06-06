package com.b_lam.resplash.ui.autowallpaper.collections

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.ui.base.BaseFragment
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.showSnackBar
import kotlinx.android.synthetic.main.empty_error_state_layout.view.*
import kotlinx.android.synthetic.main.fragment_selected_auto_wallpaper_collection.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectedAutoWallpaperCollectionFragment :
    BaseFragment(), AutoWallpaperCollectionListAdapter.ItemEventCallback {

    override val layoutId =  R.layout.fragment_selected_auto_wallpaper_collection

    private val sharedViewModel: AutoWallpaperCollectionViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedCollectionsAdapter =
            AutoWallpaperCollectionListAdapter(AutoWallpaperCollectionListAdapter.ItemType.SELECTED, this)

        recycler_view.apply {
            adapter = selectedCollectionsAdapter
            layoutManager = LinearLayoutManager(context).apply {
                addItemDecoration(SpacingItemDecoration(context, R.dimen.keyline_7))
            }
        }

        with(sharedViewModel) {
            selectedAutoWallpaperCollections.observe(viewLifecycleOwner) {
                selectedCollectionsAdapter.submitList(it)
            }
            numCollectionsLiveData.observe(viewLifecycleOwner) {
                empty_state_layout.isVisible = it == 0
                recycler_view.isVisible = it != 0
            }
        }

        setEmptyStateText()
    }

    override fun onCollectionClick(id: Int) {
        Intent(context, CollectionDetailActivity::class.java).apply {
            putExtra(CollectionDetailActivity.EXTRA_COLLECTION_ID, id.toString())
            startActivity(this)
        }
    }

    override fun onAddClick(collection: AutoWallpaperCollection) {
        // Do nothing
    }

    override fun onRemoveClick(id: Int) {
        sharedViewModel.removeAutoWallpaperCollection(id)
        recycler_view.showSnackBar(R.string.auto_wallpaper_collection_removed)
    }

    private fun setEmptyStateText() {
        empty_state_layout.empty_error_state_title.text = getString(R.string.empty_state_title)
        empty_state_layout.empty_error_state_subtitle.text = getString(R.string.auto_wallpaper_no_selected_collections)
    }

    companion object {

        fun newInstance() = SelectedAutoWallpaperCollectionFragment()
    }
}
