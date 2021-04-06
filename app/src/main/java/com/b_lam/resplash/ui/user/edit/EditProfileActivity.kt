package com.b_lam.resplash.ui.user.edit

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.widget.doOnTextChanged
import by.kirich1409.viewbindingdelegate.viewBinding
import com.b_lam.resplash.R
import com.b_lam.resplash.data.user.model.Me
import com.b_lam.resplash.databinding.ActivityEditProfileBinding
import com.b_lam.resplash.ui.base.BaseActivity
import com.b_lam.resplash.util.Result
import com.b_lam.resplash.util.setupActionBar
import com.b_lam.resplash.util.showSnackBar
import com.b_lam.resplash.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : BaseActivity(R.layout.activity_edit_profile) {

    override val viewModel: EditProfileViewModel by viewModel()

    override val binding: ActivityEditProfileBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            setupActionBar(R.id.toolbar) {
                title = getString(R.string.edit_profile)
                setDisplayHomeAsUpEnabled(true)
            }

            viewModel.userLiveData.observe(this@EditProfileActivity) { result ->
                when (result) {
                    is Result.Success -> setUserInfo(result.value)
                    else -> {
                        toast(R.string.oops)
                        finish()
                    }
                }
            }

            viewModel.updatedUserLiveData.observe(this@EditProfileActivity) { result ->
                when {
                    result is Result.Success -> {
                        setUserInfo(result.value)
                        saveButton.showSnackBar(R.string.account_updated)
                    }
                    result is Result.Error && result.code == 422 -> {
                        usernameTextInputLayout.error =
                            getString(R.string.username_taken_error)
                    }
                    else -> saveButton.showSnackBar(R.string.oops)
                }
            }

            usernameTextInputLayout.editText?.doOnTextChanged { username, _, _, _ ->
                username?.let {
                    usernameTextInputLayout.error = when {
                        username.isBlank() -> getString(R.string.blank_username_error)
                        !"^[A-Za-z0-9_]*$".toRegex()
                            .matches(username) -> getString(R.string.invalid_username_error)
                        else -> null
                    }
                }
            }

            firstNameTextInputLayout.editText?.doOnTextChanged { firstName, _, _, _ ->
                firstName?.let {
                    firstNameTextInputLayout.error = when {
                        firstName.isBlank() -> getString(R.string.blank_first_name_error)
                        else -> null
                    }
                }
            }

            emailTextInputLayout.editText?.doOnTextChanged { email, _, _, _ ->
                email?.let {
                    emailTextInputLayout.error = when {
                        email.isBlank() -> getString(R.string.blank_email_error)
                        !Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches() -> getString(R.string.invalid_email_error)
                        else -> null
                    }
                }
            }

            saveButton.setOnClickListener {
                if (isInputValid()) {
                    viewModel.updateUserProfile(
                        usernameTextInputLayout.editText?.text.toString(),
                        firstNameTextInputLayout.editText?.text.toString(),
                        lastNameTextInputLayout.editText?.text.toString(),
                        emailTextInputLayout.editText?.text.toString(),
                        portfolioTextInputLayout.editText?.text.toString(),
                        instagramTextInputLayout.editText?.text.toString(),
                        locationTextInputLayout.editText?.text.toString(),
                        bioTextInputLayout.editText?.text.toString()
                    )
                } else {
                    when {
                        usernameTextInputLayout.error != null ->
                            usernameTextInputLayout.scrollToView()
                        firstNameTextInputLayout.error != null ->
                            firstNameTextInputLayout.scrollToView()
                        emailTextInputLayout.error != null ->
                            emailTextInputLayout.scrollToView()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            with(binding) {
                putString(USERNAME_KEY, usernameTextInputLayout.editText?.text.toString())
                putString(FIRST_NAME_KEY, firstNameTextInputLayout.editText?.text.toString())
                putString(LAST_NAME_KEY, lastNameTextInputLayout.editText?.text.toString())
                putString(EMAIL_KEY, emailTextInputLayout.editText?.text.toString())
                putString(PORTFOLIO_KEY, portfolioTextInputLayout.editText?.text.toString())
                putString(INSTAGRAM_USERNAME_KEY, instagramTextInputLayout.editText?.text.toString())
                putString(LOCATION_KEY, locationTextInputLayout.editText?.text.toString())
                putString(BIO_KEY, bioTextInputLayout.editText?.text.toString())
            }
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        with(binding) {
            usernameTextInputLayout.editText?.setText(savedInstanceState.getString(USERNAME_KEY))
            firstNameTextInputLayout.editText?.setText(savedInstanceState.getString(FIRST_NAME_KEY))
            lastNameTextInputLayout.editText?.setText(savedInstanceState.getString(LAST_NAME_KEY))
            emailTextInputLayout.editText?.setText(savedInstanceState.getString(EMAIL_KEY))
            portfolioTextInputLayout.editText?.setText(savedInstanceState.getString(PORTFOLIO_KEY))
            instagramTextInputLayout.editText?.setText(savedInstanceState.getString(INSTAGRAM_USERNAME_KEY))
            locationTextInputLayout.editText?.setText(savedInstanceState.getString(LOCATION_KEY))
            bioTextInputLayout.editText?.setText(savedInstanceState.getString(BIO_KEY))
        }
    }

    private fun setUserInfo(me: Me) {
        with(binding) {
            usernameTextInputLayout.editText?.setText(me.username)
            firstNameTextInputLayout.editText?.setText(me.first_name)
            lastNameTextInputLayout.editText?.setText(me.last_name)
            emailTextInputLayout.editText?.setText(me.email)
            portfolioTextInputLayout.editText?.setText(me.portfolio_url)
            instagramTextInputLayout.editText?.setText(me.instagram_username)
            locationTextInputLayout.editText?.setText(me.location)
            bioTextInputLayout.editText?.setText(me.bio)
        }
    }

    private fun isInputValid() =
        isUsernameValid(binding.usernameTextInputLayout.editText?.text.toString()) &&
                isFirstNameValid(binding.firstNameTextInputLayout.editText?.text.toString()) &&
                isEmailValid(binding.emailTextInputLayout.editText?.text.toString()) &&
                isBioValid(binding.bioTextInputLayout.editText?.text.toString())

    private fun isUsernameValid(username: String) =
        username.isNotBlank() && "^[A-Za-z0-9_]*$".toRegex().matches(username)

    private fun isFirstNameValid(firstName: String) = firstName.isNotBlank()

    private fun isEmailValid(email: String) =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isBioValid(bio: String) = bio.length <= 250

    private fun View.scrollToView(){
        handler.post {
            binding.scrollView.smoothScrollTo(0, this.top)
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
