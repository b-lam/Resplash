package com.b_lam.resplash.ui.debug

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.databinding.ActivityDebugBinding
import com.b_lam.resplash.domain.login.AccessTokenProvider.Companion.DEBUG_APP_ID_KEY
import com.b_lam.resplash.domain.login.AccessTokenProvider.Companion.DEBUG_APP_SECRET_KEY
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast

class DebugActivity : BaseActivity(R.layout.activity_debug) {

    override val viewModel: ViewModel? = null

    override val binding: ActivityDebugBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupActionBar(R.id.toolbar) {
            title = "Debug"
            setDisplayHomeAsUpEnabled(true)
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val accessKey = sharedPreferences.getString(DEBUG_APP_ID_KEY, null)
        val secretKey = sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null)

        binding.accessKeyTextField.editText?.setText(accessKey)
        binding.secretKeyTextField.editText?.setText(secretKey)

        binding.saveButton.setOnClickListener {
            sharedPreferences.edit().putString(DEBUG_APP_ID_KEY, binding.accessKeyTextField.editText?.text.toString()).apply()
            sharedPreferences.edit().putString(DEBUG_APP_SECRET_KEY, binding.secretKeyTextField.editText?.text.toString()).apply()
            toast("Saved", Toast.LENGTH_SHORT)
        }
    }
}
