package com.b_lam.resplash.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.app.NavUtils
import androidx.lifecycle.observe
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.setupActionBar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity() {

    override val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupActionBar(R.id.toolbar) {
            setTitle(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }

    override fun onBackPressed() {
        if (viewModel.shouldRestartMainActivity) {
            NavUtils.navigateUpFromSameTask(this)
        } else {
            super.onBackPressed()
        }
    }

    class SettingsFragment :
        PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val sharedViewModel: SettingsViewModel by sharedViewModel()

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val clearCachePreference = findPreference<Preference>("clear_cache")
            with(sharedViewModel) {
                glideCacheSize.observe(viewLifecycleOwner) { cacheSize ->
                    clearCachePreference?.summary = "Cache size: $cacheSize MB"
                }
                clearCachePreference?.setOnPreferenceClickListener {
                    launchClearCache()
                    true
                }
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == "layout" || key == "load_quality") {
                sharedViewModel.shouldRestartMainActivity = true
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
    }
}