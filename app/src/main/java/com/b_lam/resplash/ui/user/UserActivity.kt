package com.b_lam.resplash.ui.user

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.user.model.User
import com.b_lam.resplash.databinding.ActivityUserBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.util.*
import com.b_lam.resplash.util.customtabs.CustomTabsHelper
import com.b_lam.resplash.util.livedata.observeEvent
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserActivity : BaseActivity(R.layout.activity_user) {

    override val viewModel: UserViewModel by viewModel()

    override val binding: ActivityUserBinding by viewBinding()

    private lateinit var fragmentPagerAdapter: UserFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = intent.getParcelableExtra<User>(EXTRA_USER)
        val username = intent.getStringExtra(EXTRA_USERNAME)

        when {
            user != null -> viewModel.setUser(user)
            username != null -> viewModel.getUser(username)
            else -> finish()
        }

        viewModel.userLiveData.observe(this) { setup(it) }
        viewModel.getUserResultLiveData.observeEvent(this) {
            if (it !is Result.Success) {
                toast(R.string.oops)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_open_portfolio_link)?.isVisible =
            !viewModel.userLiveData.value?.portfolio_url.isNullOrBlank()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_portfolio_link -> {
                viewModel.userLiveData.value?.portfolio_url?.let { openUrlInBrowser(it) }
                true
            }
            R.id.action_open_in_browser -> {
                viewModel.userLiveData.value?.links?.html?.let { openUrlInBrowser(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setup(user: User) = with(binding) {
        setupActionBar(R.id.toolbar) {
            title = user.username
            setDisplayHomeAsUpEnabled(true)
        }
        fragmentPagerAdapter = UserFragmentPagerAdapter(this@UserActivity, supportFragmentManager, user)
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
        toolbar.setOnClickListener { appBar.setExpanded(true) }
        invalidateOptionsMenu()

        contentLoadingLayout.hide()
        userContentLayout.isVisible = true

        userImageView.loadProfilePicture(user)
        userNameTextView.text = user.name

        photosCountTextView.text = user.total_photos?.toPrettyString()
        likesCountTextView.text = user.total_likes?.toPrettyString()
        collectionsCountTextView.text = user.total_collections?.toPrettyString()

        photosCountContainer.setOnClickListener { goToTab(UserFragmentPagerAdapter.UserFragment.PHOTO) }
        likesCountContainer.setOnClickListener { goToTab(UserFragmentPagerAdapter.UserFragment.LIKES) }
        collectionsCountContainer.setOnClickListener { goToTab(UserFragmentPagerAdapter.UserFragment.COLLECTION) }

        locationTextView.setTextAndVisibility(user.location)
        locationTextView.setOnClickListener { openLocationInMaps(user.location) }

        bioTextView.setTextAndVisibility(user.bio?.trimEnd())

        user.username?.let { username -> viewModel.getUserListings(username) }
    }

    private fun openUrlInBrowser(url: String) {
        CustomTabsHelper.openCustomTab(this, Uri.parse(url), sharedPreferencesRepository.theme)
    }

    private fun goToTab(type: UserFragmentPagerAdapter.UserFragment) {
        val position = fragmentPagerAdapter.getFragmentIndexOfType(type)
        if (position != -1) {
            binding.viewPager.currentItem = position
            binding.appBar.setExpanded(false)
        }
    }

    private class UserFragmentPagerAdapter(
        private val context: Context,
        private val fm: FragmentManager,
        user: User
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentTypes = mutableListOf<UserFragment>()
        private val fragmentTags = SparseArray<String>()

        init {
            if (user.total_photos != 0) fragmentTypes.add(UserFragment.PHOTO)
            if (user.total_likes != 0) fragmentTypes.add(UserFragment.LIKES)
            if (user.total_collections != 0) fragmentTypes.add(UserFragment.COLLECTION)
        }

        enum class UserFragment(val titleRes: Int) {
            PHOTO(R.string.photos),
            LIKES(R.string.likes),
            COLLECTION(R.string.collections)
        }

        fun getFragment(position: Int) =
            fm.findFragmentByTag(fragmentTags.get(position)) as? BaseSwipeRecyclerViewFragment<*, *>

        fun getItemType(position: Int) = fragmentTypes[position]

        fun getFragmentIndexOfType(type: UserFragment) = fragmentTypes.indexOf(type)

        override fun getItem(position: Int): Fragment {
            return when (getItemType(position)) {
                UserFragment.PHOTO -> UserPhotoFragment.newInstance()
                UserFragment.LIKES -> UserLikesFragment.newInstance()
                UserFragment.COLLECTION -> UserCollectionFragment.newInstance()
            }
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position)
            (fragment as? Fragment)?.tag?.let { fragmentTags.put(position, it) }
            return fragment
        }

        override fun getPageTitle(position: Int) = context.getString(getItemType(position).titleRes)

        override fun getCount() = fragmentTypes.size
    }

    companion object {

        const val EXTRA_USER = "extra_user"
        const val EXTRA_USERNAME = "extra_username"
    }
}
