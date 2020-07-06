package com.b_lam.resplash.ui.search

import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.observe
import androidx.viewpager.widget.ViewPager
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.util.focusAndShowKeyboard
import com.b_lam.resplash.util.hideKeyboard
import com.b_lam.resplash.util.setupActionBar
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_search.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : BaseActivity() {

    override val viewModel: SearchViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setupActionBar(R.id.toolbar) {
            title = ""
            setDisplayHomeAsUpEnabled(true)
        }

        intent.extras?.getString(EXTRA_SEARCH_QUERY)?.let {
            viewModel.updateQuery(it)
            search_text_input_layout.editText?.apply {
                setText(it)
                setSelection(text.length)
            }
        }

        val fragmentPagerAdapter = SearchFragmentPagerAdapter(this, supportFragmentManager)
        view_pager.apply {
            adapter = fragmentPagerAdapter
            offscreenPageLimit = 2
        }
        tab_layout.apply {
            setupWithViewPager(view_pager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    fragmentPagerAdapter.getFragment(tab?.position ?: 0)?.scrollToTop()
                }
            })
        }
        search_text_input_layout.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.updateQuery(search_text_input_layout.editText?.text.toString())
                currentFocus?.hideKeyboard()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        if (search_text_input_layout.editText?.text.isNullOrBlank()) {
            search_text_input_layout.editText?.focusAndShowKeyboard()
        }
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                filter_button.setVisibility(fragmentPagerAdapter.getItemType(position) ==
                        SearchFragmentPagerAdapter.SearchFragment.PHOTO &&
                        !viewModel.queryLiveData.value.isNullOrBlank())
            }
        })
        viewModel.queryLiveData.observe(this) { filter_button.setVisibility(
            fragmentPagerAdapter.getItemType(view_pager.currentItem) ==
                    SearchFragmentPagerAdapter.SearchFragment.PHOTO && it.isNotBlank())
        }
        filter_button.setOnClickListener { showFilterBottomSheet() }
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
            fm.findFragmentByTag(fragmentTags.get(position)) as? BaseSwipeRecyclerViewFragment<*>

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
