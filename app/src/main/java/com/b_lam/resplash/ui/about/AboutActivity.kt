package com.b_lam.resplash.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivityAboutBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.setupActionBar
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

@SuppressLint("SetTextI18n")
class AboutActivity : BaseActivity(R.layout.activity_about) {

    override val viewModel: ViewModel? = null

    override val binding: ActivityAboutBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.about)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.appVersion.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, AboutFragment())
            .commit()
    }

    class AboutFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.about_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            findPreference<Preference>("licenses")?.setOnPreferenceClickListener {
                context?.let {
                    Intent(it, OssLicensesMenuActivity::class.java).apply {
                        OssLicensesMenuActivity.setActivityTitle(getString(R.string.licenses))
                        startActivity(this)
                    }
                }
                true
            }
        }
    }
}
