package com.b_lam.resplash.ui.autowallpaper.collections

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivityAutoWallpaperCollectionBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.setupActionBar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AutoWallpaperCollectionActivity : BaseActivity(R.layout.activity_auto_wallpaper_collection) {

    override val viewModel: AutoWallpaperCollectionViewModel by viewModel()

    override val binding: ActivityAutoWallpaperCollectionBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.auto_wallpaper_source_collections)
            setDisplayHomeAsUpEnabled(true)
        }

        val fragmentPagerAdapter =
            AutoWallpaperCollectionsFragmentPagerAdapter(this, supportFragmentManager)
        binding.viewPager.adapter = fragmentPagerAdapter
        binding.tabLayout.apply {
            setupWithViewPager(binding.viewPager)
            for (tabPosition in 0 until tabCount) {
                getTabAt(tabPosition)?.setIcon(fragmentPagerAdapter.getPageIcon(tabPosition))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_auto_wallpaper_collection, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_collection_from_url -> {
                showAddCollectionBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddCollectionBottomSheet() {
        AddAutoWallpaperCollectionBottomSheet
            .newInstance()
            .show(supportFragmentManager, AddAutoWallpaperCollectionBottomSheet.TAG)
    }

    private class AutoWallpaperCollectionsFragmentPagerAdapter(
        private val context: Context,
        fm: FragmentManager
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        enum class AutoWallpaperCollectionsFragment(val titleRes: Int, val iconRes: Int) {
            SELECTED(R.string.auto_wallpaper_selected_collections, R.drawable.ic_dashboard_24dp),
            DISCOVER(R.string.auto_wallpaper_discover, R.drawable.ic_search_24dp)
        }

        private fun getItemType(position: Int) = AutoWallpaperCollectionsFragment.values()[position]

        override fun getItem(position: Int): Fragment = when (getItemType(position)) {
            AutoWallpaperCollectionsFragment.SELECTED -> SelectedAutoWallpaperCollectionFragment.newInstance()
            AutoWallpaperCollectionsFragment.DISCOVER -> DiscoverAutoWallpaperCollectionFragment.newInstance()
        }

        fun getPageIcon(position: Int) = getItemType(position).iconRes

        override fun getPageTitle(position: Int) = context.getString(getItemType(position).titleRes)

        override fun getCount() = AutoWallpaperCollectionsFragment.values().size
    }
}
