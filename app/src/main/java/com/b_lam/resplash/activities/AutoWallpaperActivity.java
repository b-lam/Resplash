package com.b_lam.resplash.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.b_lam.resplash.R;
import com.b_lam.resplash.data.service.AutoWallpaperService;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AutoWallpaperActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_auto_wallpaper) Toolbar toolbar;

    private final static String TAG = "AutoWallpaperActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.LIGHT:
                setTheme(R.style.PreferenceThemeLight);
                break;
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.PreferenceThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.PreferenceThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_auto_wallpaper);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.auto_wallpaper_title);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auto_wallpaper_fragment_container, new AutoWallpaperFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class AutoWallpaperFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private EditTextPreference customCategoryPreference;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.autowallpaperpreferences, rootKey);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);
            enableAutoWallpaper(autoWallpaperEnabled);

            customCategoryPreference = (EditTextPreference) findPreference("auto_wallpaper_custom_category");
            boolean customCategorySelected = sharedPreferences
                    .getString("auto_wallpaper_category", getString(R.string.auto_wallpaper_category_default))
                    .equals("Custom");
            showCustomCategoryPreference(customCategorySelected);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("auto_wallpaper")) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("auto_wallpaper");
                enableAutoWallpaper(checkBoxPreference.isChecked());
            } else if (preference.getKey().equals("auto_wallpaper_history")) {
                final Intent intent = new Intent(getContext(), WallpaperHistoryActivity.class);
                startActivity(intent);
            }

            return super.onPreferenceTreeClick(preference);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("auto_wallpaper_category")) {
                boolean customCategorySelected = sharedPreferences
                        .getString("auto_wallpaper_category", getString(R.string.auto_wallpaper_category_default))
                        .equals("Custom");
                showCustomCategoryPreference(customCategorySelected);
            }

            if (key.contains("auto_wallpaper")) {
                scheduleAutoWallpaperJob(sharedPreferences);
            }
        }

        private void enableAutoWallpaper(boolean enable) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("auto_wallpaper");
            if (enable) {
                checkBoxPreference.setTitle(R.string.on);
            } else {
                checkBoxPreference.setTitle(R.string.off);
            }
        }

        private void showCustomCategoryPreference(boolean show) {
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("auto_wallpaper_source");
            if (show) {
                preferenceCategory.addPreference(customCategoryPreference);
            } else {
                preferenceCategory.removePreference(customCategoryPreference);
            }
        }

        private void scheduleAutoWallpaperJob(SharedPreferences sharedPreferences) {
            boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);

            if (autoWallpaperEnabled) {
                boolean deviceOnWifiCondition = sharedPreferences.getBoolean("auto_wallpaper_on_wifi", true);
                boolean deviceChargingCondition = sharedPreferences.getBoolean("auto_wallpaper_charging", true);
                boolean deviceIdleCondition = sharedPreferences.getBoolean("auto_wallpaper_idle", true);
                String changeWallpaperInterval = sharedPreferences.getString("auto_wallpaper_interval", getString(R.string.auto_wallpaper_interval_default));
                long changeWallpaperIntervalMillis = TimeUnit.MINUTES.toMillis(Long.valueOf(changeWallpaperInterval));

                JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

                JobInfo.Builder builder = new JobInfo.Builder(AutoWallpaperService.AUTO_WALLPAPER_JOB_ID,
                        new ComponentName(getContext(), AutoWallpaperService.class));

                if (deviceOnWifiCondition) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                } else {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                }

                builder.setRequiresCharging(deviceChargingCondition);
                builder.setRequiresDeviceIdle(deviceIdleCondition);
                builder.setPeriodic(changeWallpaperIntervalMillis);

                String category = sharedPreferences.getString("auto_wallpaper_category",
                        getString(R.string.auto_wallpaper_category_default));
                PersistableBundle extras = new PersistableBundle();

                if (category.equals("Featured")) {
                    extras.putBoolean(AutoWallpaperService.AUTO_WALLPAPER_CATEGORY_FEATURED_KEY, true);
                } else if (category.equals("Custom")) {
                    extras.putString(AutoWallpaperService.AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY,
                            sharedPreferences.getString("auto_wallpaper_custom_category",
                                    getString(R.string.auto_wallpaper_custom_category_default)));
                }

                extras.putString(AutoWallpaperService.AUTO_WALLPAPER_QUALITY_KEY,
                        sharedPreferences.getString("wallpaper_quality", "Full"));

                extras.putString(AutoWallpaperService.AUTO_WALLPAPER_THUMBNAIL_KEY,
                        sharedPreferences.getString("load_quality", "Regular"));

                builder.setExtras(extras);

                if (jobScheduler != null) {
                    jobScheduler.cancel(AutoWallpaperService.AUTO_WALLPAPER_JOB_ID);
                    jobScheduler.schedule(builder.build());
                }
            }
        }
    }
}
