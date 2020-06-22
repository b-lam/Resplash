package com.b_lam.resplash.ui.user.edit

import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.observe
import com.b_lam.resplash.R
import com.b_lam.resplash.data.user.model.Me
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.showSnackBar
import com.b_lam.resplash.util.toast
import kotlinx.android.synthetic.main.activity_edit_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : BaseActivity() {

    override val viewModel: EditProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        setupActionBar(R.id.toolbar) {
            title = getString(R.string.edit_profile)
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.userLiveData.observe(this) { result ->
            when (result) {
                is Result.Success -> setUserInfo(result.value)
                else -> {
                    toast(R.string.oops)
                    finish()
                }
            }
        }

        viewModel.updatedUserLiveData.observe(this) { result ->
            when {
                result is Result.Success -> {
                    setUserInfo(result.value)
                    save_button.showSnackBar(R.string.account_updated)
                }
                result is Result.Error && result.code == 422 -> {
                    username_text_input_layout.error = getString(R.string.username_taken_error)
                }
                else -> save_button.showSnackBar(R.string.oops)
            }
        }

        username_text_input_layout.editText?.doOnTextChanged { username, _, _, _ ->
            username?.let {
                username_text_input_layout.error = when {
                    username.isBlank() -> getString(R.string.blank_username_error)
                    !"^[A-Za-z0-9_]*$".toRegex().matches(username) -> getString(R.string.invalid_username_error)
                    else -> null
                }
            }
        }

        first_name_text_input_layout.editText?.doOnTextChanged { firstName, _, _, _ ->
            firstName?.let {
                first_name_text_input_layout.error = when {
                    firstName.isBlank() -> getString(R.string.blank_first_name_error)
                    else -> null
                }
            }
        }

        email_text_input_layout.editText?.doOnTextChanged { email, _, _, _ ->
            email?.let {
                email_text_input_layout.error = when {
                    email.isBlank() -> getString(R.string.blank_email_error)
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.invalid_email_error)
                    else -> null
                }
            }
        }

        save_button.setOnClickListener {
            if (isInputValid()) {
                viewModel.updateUserProfile(
                    username_text_input_layout.editText?.text.toString(),
                    first_name_text_input_layout.editText?.text.toString(),
                    last_name_text_input_layout.editText?.text.toString(),
                    email_text_input_layout.editText?.text.toString(),
                    portfolio_text_input_layout.editText?.text.toString(),
                    instagram_text_input_layout.editText?.text.toString(),
                    location_text_input_layout.editText?.text.toString(),
                    bio_text_input_layout.editText?.text.toString()
                )
            } else {
                when {
                    username_text_input_layout.error != null -> username_text_input_layout.scrollToView()
                    first_name_text_input_layout.error != null -> first_name_text_input_layout.scrollToView()
                    email_text_input_layout.error != null -> email_text_input_layout.scrollToView()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            putString(USERNAME_KEY, username_text_input_layout.editText?.text.toString())
            putString(FIRST_NAME_KEY, first_name_text_input_layout.editText?.text.toString())
            putString(LAST_NAME_KEY, last_name_text_input_layout.editText?.text.toString())
            putString(EMAIL_KEY, email_text_input_layout.editText?.text.toString())
            putString(PORTFOLIO_KEY, portfolio_text_input_layout.editText?.text.toString())
            putString(INSTAGRAM_USERNAME_KEY, instagram_text_input_layout.editText?.text.toString())
            putString(LOCATION_KEY, location_text_input_layout.editText?.text.toString())
            putString(BIO_KEY, bio_text_input_layout.editText?.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        username_text_input_layout.editText?.setText(savedInstanceState.getString(USERNAME_KEY))
        first_name_text_input_layout.editText?.setText(savedInstanceState.getString(FIRST_NAME_KEY))
        last_name_text_input_layout.editText?.setText(savedInstanceState.getString(LAST_NAME_KEY))
        email_text_input_layout.editText?.setText(savedInstanceState.getString(EMAIL_KEY))
        portfolio_text_input_layout.editText?.setText(savedInstanceState.getString(PORTFOLIO_KEY))
        instagram_text_input_layout.editText?.setText(savedInstanceState.getString(INSTAGRAM_USERNAME_KEY))
        location_text_input_layout.editText?.setText(savedInstanceState.getString(LOCATION_KEY))
        bio_text_input_layout.editText?.setText(savedInstanceState.getString(BIO_KEY))
    }

    private fun setUserInfo(me: Me) {
        username_text_input_layout.editText?.setText(me.username)
        first_name_text_input_layout.editText?.setText(me.first_name)
        last_name_text_input_layout.editText?.setText(me.last_name)
        email_text_input_layout.editText?.setText(me.email)
        portfolio_text_input_layout.editText?.setText(me.portfolio_url)
        instagram_text_input_layout.editText?.setText(me.instagram_username)
        location_text_input_layout.editText?.setText(me.location)
        bio_text_input_layout.editText?.setText(me.bio)
    }

    private fun isInputValid() =
        isUsernameValid(username_text_input_layout.editText?.text.toString()) &&
                isFirstNameValid(first_name_text_input_layout.editText?.text.toString()) &&
                isEmailValid(email_text_input_layout.editText?.text.toString()) &&
                isBioValid(bio_text_input_layout.editText?.text.toString())

    private fun isUsernameValid(username: String) =
        username.isNotBlank() && "^[A-Za-z0-9_]*$".toRegex().matches(username)

    private fun isFirstNameValid(firstName: String) = firstName.isNotBlank()

    private fun isEmailValid(email: String) =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isBioValid(bio: String) = bio.length <= 250

    private fun View.scrollToView(){
        Handler().post {
            scroll_view.smoothScrollTo(0, this.top)
        }
    }

    companion object {

        const val EXTRA_USERNAME = "extra_username"

        private const val USERNAME_KEY = "username_key"
        private const val FIRST_NAME_KEY = "first_name_key"
        private const val LAST_NAME_KEY = "last_name_key"
        private const val EMAIL_KEY = "email_key"
        private const val PORTFOLIO_KEY = "portfolio_key"
        private const val INSTAGRAM_USERNAME_KEY = "instagram_username_key"
        private const val LOCATION_KEY = "location_key"
        private const val BIO_KEY = "bio_key"
    }
}
