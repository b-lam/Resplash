package com.b_lam.resplash.ui.muzei

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.autowallpaper.collections.AutoWallpaperCollectionActivity
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.upgrade.UpgradeActivity
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast
import com.b_lam.resplash.worker.AutoWallpaperWorker
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MuzeiSettingsActivity : BaseActivity() {

    override val viewModel: MuzeiSettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei_settings)

        setupActionBar(R.id.toolbar) {
            setTitle(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private val sharedViewModel: MuzeiSettingsViewModel by sharedViewModel()

        private val sharedPreferencesRepository: SharedPreferencesRepository by inject()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.muzei_preference, rootKey)

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

            findPreference<Preference>("auto_wallpaper_collections")?.setOnPreferenceClickListener {
                startActivity(Intent(context, AutoWallpaperCollectionActivity::class.java))
                true
            }

            sharedViewModel.resplashProLiveData.observe(viewLifecycleOwner) {
                if (it?.entitled != true) {
                    resetCustomSourceIfNotEntitled()
                }
            }
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