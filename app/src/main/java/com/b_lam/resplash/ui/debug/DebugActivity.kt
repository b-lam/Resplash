package com.b_lam.resplash.ui.debug

import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.login.AccessTokenProvider.Companion.DEBUG_APP_ID_KEY
import com.b_lam.resplash.domain.login.AccessTokenProvider.Companion.DEBUG_APP_SECRET_KEY
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.ui.base.BaseViewModel
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.toast
import kotlinx.android.synthetic.main.activity_debug.*

class DebugActivity : BaseActivity() {

    override val viewModel: BaseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        setupActionBar(R.id.toolbar) {
            title = "Debug"
            setDisplayHomeAsUpEnabled(true)
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val accessKey = sharedPreferences.getString(DEBUG_APP_ID_KEY, null)
        val secretKey = sharedPreferences.getString(DEBUG_APP_SECRET_KEY, null)

        access_key_text_field.editText?.setText(accessKey)
        secret_key_text_field.editText?.setText(secretKey)

        save_button.setOnClickListener {
            sharedPreferences.edit().putString(DEBUG_APP_ID_KEY, access_key_text_field.editText?.text.toString()).apply()
            sharedPreferences.edit().putString(DEBUG_APP_SECRET_KEY, secret_key_text_field.editText?.text.toString()).apply()
            toast("Saved", Toast.LENGTH_SHORT)
        }
    }
}
