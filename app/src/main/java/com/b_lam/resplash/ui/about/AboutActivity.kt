package com.b_lam.resplash.ui.about

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.b_lam.resplash.BuildConfig
import com.b_lam.resplash.R
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.setupActionBar
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_about.*

@SuppressLint("SetTextI18n")
class AboutActivity : BaseActivity() {

    override val viewModel: BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.about)
            setDisplayHomeAsUpEnabled(true)
        }

        app_version.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

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
