package com.b_lam.resplash.ui.base

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.b_lam.resplash.R
import com.b_lam.resplash.domain.SharedPreferencesRepository
import com.b_lam.resplash.ui.main.MainActivity
import com.b_lam.resplash.util.applyLanguage
import com.b_lam.resplash.util.getThemeAttrColor
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {

    abstract val viewModel: BaseViewModel?

    val sharedPreferencesRepository: SharedPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setRecentAppsHeaderColor()
        applyLanguage(sharedPreferencesRepository.locale)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyLanguage(sharedPreferencesRepository.locale)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.applyLanguage(sharedPreferencesRepository.locale))
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
}