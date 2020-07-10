package com.b_lam.resplash.ui.autowallpaper

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.text.util.LinkifyCompat
import androidx.lifecycle.observe
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo.State.*
import androidx.work.WorkManager
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.domain.SharedPreferencesRepository.Companion.PREFERENCE_AUTO_WALLPAPER_ENABLE_KEY
import com.b_lam.resplash.ui.autowallpaper.collections.AutoWallpaperCollectionActivity
import com.b_lam.resplash.ui.autowallpaper.history.AutoWallpaperHistoryActivity
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.upgrade.UpgradeActivity
import com.b_lam.resplash.util.*
import com.b_lam.resplash.worker.AutoWallpaperWorker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_auto_wallpaper_settings.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AutoWallpaperSettingsActivity :
    BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override val viewModel: AutoWallpaperSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_wallpaper_settings)

        setupActionBar(R.id.toolbar) {
            setTitle(R.string.auto_wallpaper_title)
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()

        next_fab.setVisibility(sharedPreferencesRepository.autoWallpaperEnabled)

        val snackbar = Snackbar.make(root_container, R.string.setting_wallpaper, Snackbar.LENGTH_INDEFINITE)

        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData(AutoWallpaperWorker.AUTO_WALLPAPER_SINGLE_JOB_ID)
            .observe(this) {
                if (it.isNotEmpty()) {
                    when (it?.first()?.state) {
                        BLOCKED, ENQUEUED, RUNNING -> snackbar.show()
                        SUCCEEDED -> snackbar.dismiss()
                        FAILED, CANCELLED -> {
                            snackbar.dismiss()
                            root_container.showSnackBar(R.string.error_setting_wallpaper)
                        }
                    }
                }
            }

        next_fab.setOnClickListener {
            AutoWallpaperWorker.scheduleSingleAutoWallpaperJob(this, sharedPreferencesRepository)
        }

        if (isInstalledOnExternalStorage()) {
            showExternalStorageWarningDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        sharedPreferencesRepository.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferencesRepository.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_auto_wallpaper_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.auto_wallpaper_help -> {
                showHelpDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PREFERENCE_AUTO_WALLPAPER_ENABLE_KEY) {
            next_fab.setVisibility(sharedPreferencesRepository.autoWallpaperEnabled)
        }
    }

    private fun FloatingActionButton.setVisibility(visible: Boolean) {
        if (visible) { show() } else { hide() }
    }

    private fun showHelpDialog() {
        val textView = TextView(this).apply {
            val spannableString = SpannableString(getString(R.string.auto_wallpaper_help_message))
            LinkifyCompat.addLinks(spannableString, Linkify.WEB_URLS)
            text = spannableString
            movementMethod = LinkMovementMethod.getInstance()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setTextAppearance(this@AutoWallpaperSettingsActivity, android.R.style.TextAppearance_Small)
            } else {
                setTextAppearance(android.R.style.TextAppearance_Small)
            }
            setPadding(
                resources.getDimensionPixelSize(R.dimen.keyline_8),
                resources.getDimensionPixelSize(R.dimen.keyline_5),
                resources.getDimensionPixelSize(R.dimen.keyline_8),
                resources.getDimensionPixelSize(R.dimen.keyline_0)
            )
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.auto_wallpaper_help_title)
            .setView(textView)
            .setPositiveButton(R.string.ok, null)
            .create()
            .show()
    }

    private fun showExternalStorageWarningDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Installed on SD card?")
            .setMessage("Hey there! We've noticed you've decided to move this application to " +
                    "the SD card. Unfortunately, due to a limitation in Android, this means that " +
                    "the Auto Wallpaper feature might not work properly. You have been warned!")
            .setPositiveButton(R.string.ok, null)
            .create()
            .show()
    }

    class SettingsFragment :
        PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val sharedViewModel: AutoWallpaperSettingsViewModel by sharedViewModel()

        private val sharedPreferencesRepository: SharedPreferencesRepository by inject()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.auto_wallpaper_preferences, rootKey)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                findPreference<Preference>("auto_wallpaper_idle")?.isVisible = true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                findPreference<Preference>("auto_wallpaper_crop")?.isVisible = true
                findPreference<Preference>("auto_wallpaper_select_screen")?.isVisible = true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                findPreference<Preference>("auto_wallpaper_notification_settings")?.isVisible = true
                findPreference<Preference>("auto_wallpaper_show_notification")?.isVisible = false
            }

            findPreference<ListPreference>("auto_wallpaper_interval")?.summaryProvider =
                Preference.SummaryProvider<ListPreference> {
                    getString(R.string.auto_wallpaper_interval_summary, it.entry)
                }

            findPreference<ListPreference>("auto_wallpaper_select_screen")?.summaryProvider =
                Preference.SummaryProvider<ListPreference> {
                    getString(R.string.auto_wallpaper_select_screen_summary, it.entry)
                }

            findPreference<ListPreference>("auto_wallpaper_orientation")?.summaryProvider =
                Preference.SummaryProvider<ListPreference> {
                    getString(R.string.auto_wallpaper_orientation_summary, it.entry)
                }

            findPreference<ListPreference>("auto_wallpaper_content_filter")?.summaryProvider =
                Preference.SummaryProvider<ListPreference> {
                    when (it.value) {
                        "low" -> getString(R.string.auto_wallpaper_content_filter_low_summary)
                        else -> getString(R.string.auto_wallpaper_content_filter_high_summary)
                    }
                }

            findPreference<EditTextPreference>("auto_wallpaper_username")?.summaryProvider =
                Preference.SummaryProvider<EditTextPreference> {
                    if (it.text.isNullOrBlank()) {
                        getString(R.string.auto_wallpaper_source_not_set)
                    } else {
                        getString(R.string.auto_wallpaper_user_summary, it.text)
                    }
                }

            findPreference<EditTextPreference>("auto_wallpaper_search_terms")?.summaryProvider =
                Preference.SummaryProvider<EditTextPreference> {
                    if (it.text.isNullOrBlank()) {
                        getString(R.string.auto_wallpaper_source_not_set)
                    } else {
                        it.text
                    }
                }

            findPreference<ListPreference>("auto_wallpaper_source")?.summaryProvider =
                Preference.SummaryProvider<ListPreference> {
                    getString(R.string.auto_wallpaper_source_summary, it.entry)
                }

            findPreference<ListPreference>("auto_wallpaper_source")
                ?.setOnPreferenceChangeListener { _, newValue ->
                    if (AutoWallpaperWorker.Companion.Source.SOURCE_UNENTITLED.contains(newValue) ||
                        sharedViewModel.resplashProLiveData.value?.entitled == true) {
                        setCustomSourceVisibility(newValue.toString())
                        true
                    } else {
                        startActivity(Intent(context, UpgradeActivity::class.java))
                        context.toast(getString(R.string.upgrade_required))
                        false
                    }
                }

            setCustomSourceVisibility(sharedPreferencesRepository.autoWallpaperSource)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            findPreference<Preference>("auto_wallpaper_history")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AutoWallpaperHistoryActivity::class.java))
                true
            }

            findPreference<Preference>("auto_wallpaper_collections")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AutoWallpaperCollectionActivity::class.java))
                true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                findPreference<Preference>("auto_wallpaper_notification_settings")?.setOnPreferenceClickListener {
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                        putExtra(Settings.EXTRA_CHANNEL_ID, NotificationManager.NEW_AUTO_WALLPAPER_CHANNEL_ID)
                    }
                    if (intent.resolveActivity(requireContext().packageManager) != null) {
                        startActivity(intent)
                    } else {
                        context?.toast(R.string.oops)
                    }
                    true
                }
            }

            sharedViewModel.resplashProLiveData.observe(viewLifecycleOwner) {
                if (it?.entitled != true) {
                    resetCustomSourceIfNotEntitled()
                }
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key?.contains("auto_wallpaper") == true) {
                context?.let {
                    AutoWallpaperWorker.scheduleAutoWallpaperJob(it, sharedPreferencesRepository)
                }
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        private fun setCustomSourceVisibility(value: String?) {
            findPreference<Preference>("auto_wallpaper_collections")?.isVisible =
                value == AutoWallpaperWorker.Companion.Source.COLLECTIONS
            findPreference<Preference>("auto_wallpaper_username")?.isVisible =
                value == AutoWallpaperWorker.Companion.Source.USER
            findPreference<Preference>("auto_wallpaper_search_terms")?.isVisible =
                value == AutoWallpaperWorker.Companion.Source.SEARCH
        }

        private fun resetCustomSourceIfNotEntitled() {
            if (AutoWallpaperWorker.Companion.Source.SOURCE_ENTITLED
                    .contains(sharedPreferencesRepository.autoWallpaperSource)) {
                val newValue = AutoWallpaperWorker.Companion.Source.FEATURED
                val sourcePreference = findPreference<ListPreference>("auto_wallpaper_source")
                sourcePreference?.value = newValue
                sourcePreference?.callChangeListener(newValue)
            }
        }
    }
}