package com.b_lam.resplash.ui.base

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.login.LoginActivity
import com.b_lam.resplash.ui.main.MainActivity
import com.b_lam.resplash.util.getThemeAttrColor
import com.b_lam.resplash.util.livedata.observeEvent
import org.koin.android.ext.android.inject
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    abstract val viewModel: BaseViewModel?

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRecentAppsHeaderColor()

        viewModel?.authRequiredLiveData?.observeEvent(this) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot && this !is MainActivity) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.applyLanguage())
    }

    private fun AppCompatActivity.setRecentAppsHeaderColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val taskDescription = TaskDescription(
                getString(R.string.app_name),
                R.mipmap.ic_launcher,
                getThemeAttrColor(this, R.attr.colorSurface)
            )
            setTaskDescription(taskDescription)
        } else {
            val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val taskDescription = TaskDescription(
                getString(R.string.app_name),
                icon,
                getThemeAttrColor(this, R.attr.colorSurface)
            )
            setTaskDescription(taskDescription)
            icon?.recycle()
        }
    }

    private fun Context.applyLanguage(): Context {
        val locale = sharedPreferencesRepository.locale
        val configuration = resources.configuration
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }
}