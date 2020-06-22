package com.b_lam.resplash.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.collection.CollectionDataSource
import com.b_lam.resplash.domain.photo.PhotoDataSource
import com.b_lam.resplash.ui.about.AboutActivity
import com.b_lam.resplash.ui.autowallpaper.AutoWallpaperSettingsActivity
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseSwipeRecyclerViewFragment
import com.b_lam.resplash.ui.debug.DebugActivity
import com.b_lam.resplash.ui.donation.DonationActivity
import com.b_lam.resplash.ui.login.LoginActivity
import com.b_lam.resplash.ui.search.SearchActivity
import com.b_lam.resplash.ui.settings.SettingsActivity
import com.b_lam.resplash.ui.upgrade.UpgradeActivity
import com.b_lam.resplash.ui.user.UserActivity
import com.b_lam.resplash.ui.user.edit.EditProfileActivity
import com.b_lam.resplash.util.customtabs.CustomTabsHelper
import com.b_lam.resplash.util.livedata.observeEvent
import com.b_lam.resplash.util.loadPhotoUrl
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplay
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplayCallbacks
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.inappmessaging.model.CardMessage
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.inappmessaging.model.MessageType
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Resplash_Theme_DayNight)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bottom_app_bar)

        val fragmentPagerAdapter = MainFragmentPagerAdapter(this, supportFragmentManager)
        view_pager.adapter = fragmentPagerAdapter
        tab_layout.apply {
            setupWithViewPager(view_pager)
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {
                    fragmentPagerAdapter.getFragment(tab?.position ?: 0).scrollToTop()
                }
            })
        }

        upload_fab.setOnClickListener { openUnsplashSubmitTab() }

        viewModel.navigationItemSelectedLiveData.observeEvent(this) { onBottomNavigationDrawerItemSelected(it) }
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshUserProfile()

        val inAppMessagingDisplay = FirebaseInAppMessagingDisplay { inAppMessage, callbacks ->
            showInAppMessagingDialog(inAppMessage, callbacks)
        }
        Firebase.inAppMessaging.setMessageDisplayComponent(inAppMessagingDisplay)
    }

    override fun onDestroy() {
        super.onDestroy()
        Firebase.inAppMessaging.clearDisplayListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_debug)?.isVisible = BuildConfig.DEBUG
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                showBottomDrawer()
                true
            }
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                true
            }
            R.id.action_order -> {
                when (tab_layout.selectedTabPosition) {
                    0 -> showPhotoOrderDialog()
                    1 -> showCollectionOrderDialog()
                }
                true
            }
            R.id.action_debug -> {
                startActivity(Intent(this, DebugActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBottomDrawer() {
        MainBottomNavigationDrawer
            .newInstance()
            .show(supportFragmentManager, MainBottomNavigationDrawer.TAG)
    }

    private fun onBottomNavigationDrawerItemSelected(actionId: Int) {
        when (actionId) {
            R.id.action_add_account -> startActivity(Intent(this, LoginActivity::class.java))
            R.id.action_view_profile -> {
                Intent(this, UserActivity::class.java).apply {
                    putExtra(UserActivity.EXTRA_USERNAME, viewModel.usernameLiveData.value)
                    startActivity(this)
                }
            }
            R.id.action_edit_profile -> {
                Intent(this, EditProfileActivity::class.java).apply {
                    putExtra(EditProfileActivity.EXTRA_USERNAME, viewModel.usernameLiveData.value)
                    startActivity(this)
                }
            }
            R.id.action_log_out -> viewModel.logout()
            R.id.action_auto_wallpaper -> startActivity(Intent(this, AutoWallpaperSettingsActivity::class.java))
            R.id.action_upgrade -> startActivity(Intent(this, UpgradeActivity::class.java))
            R.id.action_donate -> startActivity(Intent(this, DonationActivity::class.java))
            R.id.action_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.action_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        (supportFragmentManager.findFragmentByTag(MainBottomNavigationDrawer.TAG)
                as? MainBottomNavigationDrawer)?.dismiss()
    }

    private fun showPhotoOrderDialog() {
        val orderOptions = enumValues<PhotoDataSource.Companion.Order>()
            .map { getString(it.titleRes) }.toTypedArray()
        val currentSelection = viewModel.photoOrderLiveData.value?.ordinal ?: 0
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(orderOptions, currentSelection) { dialog, which ->
                if (which != currentSelection) viewModel.orderPhotosBy(which)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showCollectionOrderDialog() {
        val orderOptions = enumValues<CollectionDataSource.Companion.Order>()
            .map { getString(it.titleRes) }.toTypedArray()
        val currentSelection = viewModel.collectionOrderLiveData.value?.ordinal ?: 0
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(orderOptions, currentSelection) { dialog, which ->
                if (which != currentSelection) viewModel.orderCollectionsBy(which)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showInAppMessagingDialog(
        inAppMessage: InAppMessage,
        callbacks: FirebaseInAppMessagingDisplayCallbacks
    ) {
        if (inAppMessage.messageType == MessageType.CARD) {
            val cardMessage = inAppMessage as? CardMessage
            callbacks.impressionDetected()
            val view = layoutInflater.inflate(R.layout.dialog_in_app_messaging, null)
            view.findViewById<TextView>(R.id.title_text_view)?.text = cardMessage?.title?.text
            view.findViewById<TextView>(R.id.message_text_view)?.text = cardMessage?.body?.text
            cardMessage?.portraitImageData?.imageUrl?.let {
                view.findViewById<ImageView>(R.id.header_image_view)?.loadPhotoUrl(it)
            }
            MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.cancel()
                }
                .setNeutralButton("Learn more") { dialog, _ ->
                    cardMessage?.primaryAction?.let { callbacks.messageClicked(it) }
                    startActivity(Intent(this, UpgradeActivity::class.java))
                    dialog.dismiss()
                }
                .setOnCancelListener {
                    callbacks.messageDismissed(
                        FirebaseInAppMessagingDisplayCallbacks.InAppMessagingDismissType.CLICK)
                }
                .create()
                .show()
        } else {
            callbacks.displayErrorEncountered(
                FirebaseInAppMessagingDisplayCallbacks.InAppMessagingErrorReason.UNSPECIFIED_RENDER_ERROR)
        }
    }

    private fun openUnsplashSubmitTab() {
        val uri = Uri.parse(getString(R.string.unsplash_submit_url))
        CustomTabsHelper.openCustomTab(this, uri, sharedPreferencesRepository.theme)
    }

    private class MainFragmentPagerAdapter(
        private val context: Context,
        fm: FragmentManager
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = mutableListOf<BaseSwipeRecyclerViewFragment<*>>()

        enum class MainFragment(val titleRes: Int) {
            HOME(R.string.home),
            COLLECTION(R.string.collections)
        }

        fun getFragment(position: Int) = fragments[position]

        private fun getItemType(position: Int) = MainFragment.values()[position]

        override fun getItem(position: Int): Fragment {
            val fragment = when (getItemType(position)) {
                MainFragment.HOME -> MainPhotoFragment.newInstance()
                MainFragment.COLLECTION -> MainCollectionFragment.newInstance()
            }
            fragments.add(position, fragment)
            return fragment
        }

        override fun getPageTitle(position: Int) = context.getString(getItemType(position).titleRes)

        override fun getCount() = MainFragment.values().size
    }
}
