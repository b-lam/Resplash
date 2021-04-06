package com.b_lam.resplash.ui.search

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivitySearchBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.util.focusAndShowKeyboard
import com.b_lam.resplash.util.hideKeyboard
import com.b_lam.resplash.util.setupActionBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : BaseActivity(R.layout.activity_search) {

    override val viewModel: SearchViewModel by viewModel()

    override val binding: ActivitySearchBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            setupActionBar(R.id.toolbar) {
                title = ""
                setDisplayHomeAsUpEnabled(true)
            }

            intent.extras?.getString(EXTRA_SEARCH_QUERY)?.let {
                viewModel.updateQuery(it)
                searchTextInputLayout.editText?.apply {
                    setText(it)
                    setSelection(text.length)
                }
            }

            val fragmentPagerAdapter = SearchFragmentPagerAdapter(this@SearchActivity, supportFragmentManager)
            viewPager.apply {
                adapter = fragmentPagerAdapter
                offscreenPageLimit = 2
            }
            tabLayout.apply {
                setupWithViewPager(viewPager)
                addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {}
                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                        fragmentPagerAdapter.getFragment(tab?.position ?: 0)?.scrollToTop()
                    }
                })
            }
            searchTextInputLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    viewModel.updateQuery(searchTextInputLayout.editText?.text.toString())
                    currentFocus?.hideKeyboard()
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            if (searchTextInputLayout.editText?.text.isNullOrBlank()) {
                searchTextInputLayout.editText?.focusAndShowKeyboard()
            }
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    filterButton.setVisibility(fragmentPagerAdapter.getItemType(position) ==
                            SearchFragmentPagerAdapter.SearchFragment.PHOTO &&
                            !viewModel.queryLiveData.value.isNullOrBlank())
                }
            })
            viewModel.queryLiveData.observe(this@SearchActivity) {
                filterButton.setVisibility(
                    fragmentPagerAdapter.getItemType(viewPager.currentItem) ==
                            SearchFragmentPagerAdapter.SearchFragment.PHOTO && it.isNotBlank()
                )
            }
            filterButton.setOnClickListener { showFilterBottomSheet() }
        }
    }

    private fun showFilterBottomSheet() {
        SearchPhotoFilterBottomSheet
            .newInstance()
            .show(supportFragmentManager, SearchPhotoFilterBottomSheet.TAG)
    }

    private fun ExtendedFloatingActionButton.setVisibility(visible: Boolean) {
        if (visible) show() else hide()
    }

    private class SearchFragmentPagerAdapter(
        private val context: Context,
        private val fm: FragmentManager
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentTags = SparseArray<String>()

        enum class SearchFragment(val titleRes: Int) {
            PHOTO(R.string.photos),
            COLLECTION(R.string.collections),
            USER(R.string.users)
        }

        fun getFragment(position: Int) =
            fm.findFragmentByTag(fragmentTags.get(position)) as? BaseSwipeRecyclerViewFragment<*, *>

        fun getItemType(position: Int) = SearchFragment.values()[position]

        override fun getItem(position: Int): Fragment {
            return when (getItemType(position)) {
                SearchFragment.PHOTO -> SearchPhotoFragment.newInstance()
                SearchFragment.COLLECTION -> SearchCollectionFragment.newInstance()
                SearchFragment.USER -> SearchUserFragment.newInstance()
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position)
            (fragment as? Fragment)?.tag?.let { fragmentTags.put(position, it) }
            return fragment
        }

        override fun getPageTitle(position: Int) = context.getString(getItemType(position).titleRes)

        override fun getCount() = SearchFragment.values().size
    }

    companion object {

        const val EXTRA_SEARCH_QUERY = "extra_search_query"
    }
}
