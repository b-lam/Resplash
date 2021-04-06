package com.b_lam.resplash.ui.autowallpaper.collections

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.autowallpaper.model.AutoWallpaperCollection
import com.b_lam.resplash.databinding.FragmentSelectedAutoWallpaperCollectionBinding
import com.b_lam.resplash.ui.collection.detail.CollectionDetailActivity
import com.b_lam.resplash.ui.widget.recyclerview.SpacingItemDecoration
import com.b_lam.resplash.util.showSnackBar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectedAutoWallpaperCollectionFragment :
    Fragment(R.layout.fragment_selected_auto_wallpaper_collection),
    AutoWallpaperCollectionListAdapter.ItemEventCallback {

    private val sharedViewModel: AutoWallpaperCollectionViewModel by sharedViewModel()

    private val binding: FragmentSelectedAutoWallpaperCollectionBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedCollectionsAdapter =
            AutoWallpaperCollectionListAdapter(AutoWallpaperCollectionListAdapter.ItemType.SELECTED, this)

        binding.recyclerView.apply {
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
                binding.emptyStateLayout.root.isVisible = it == 0
                binding.recyclerView.isVisible = it != 0
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
        binding.recyclerView.showSnackBar(R.string.auto_wallpaper_collection_removed)
    }

    private fun setEmptyStateText() {
        binding.emptyStateLayout.emptyErrorStateTitle.text = getString(R.string.empty_state_title)
        binding.emptyStateLayout.emptyErrorStateSubtitle.text = getString(R.string.auto_wallpaper_no_selected_collections)
    }

    companion object {

        fun newInstance() = SelectedAutoWallpaperCollectionFragment()
    }
}
