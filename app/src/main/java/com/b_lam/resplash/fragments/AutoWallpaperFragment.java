package com.b_lam.resplash.fragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.b_lam.resplash.R;
import com.b_lam.resplash.activities.WallpaperHistoryActivity;
import com.b_lam.resplash.data.service.AutoWallpaperService;

import java.util.concurrent.TimeUnit;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class AutoWallpaperFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CheckBoxPreference mEnableAutoWallpaperPreference;
    private EditTextPreference mCustomCategoryPreference;

    private OnAutoWallpaperFragmentListener mCallback;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.autowallpaperpreferences, rootKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mEnableAutoWallpaperPreference = findPreference("auto_wallpaper");

        boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);
        enableAutoWallpaper(autoWallpaperEnabled);

        mCustomCategoryPreference = findPreference("auto_wallpaper_custom_category");
        boolean customCategorySelected = sharedPreferences
                .getString("auto_wallpaper_category", getString(R.string.auto_wallpaper_category_default))
                .equals("Custom");
        showCustomCategoryPreference(customCategorySelected);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableAutoWallpaperPreference) {
            enableAutoWallpaper(mEnableAutoWallpaperPreference.isChecked());
        } else if (preference == findPreference("auto_wallpaper_history")) {
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

    public void setOnAutoWallpaperFragmentListener(OnAutoWallpaperFragmentListener listener) {
        mCallback = listener;
    }

    private void enableAutoWallpaper(boolean enable) {
        mCallback.onAutoWallpaperEnableClicked(enable);

        if (enable) {
            mEnableAutoWallpaperPreference.setTitle(R.string.on);
        } else {
            mEnableAutoWallpaperPreference.setTitle(R.string.off);
        }
    }

    private void showCustomCategoryPreference(boolean show) {
        PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("auto_wallpaper_source");
        if (show) {
            preferenceCategory.addPreference(mCustomCategoryPreference);
        } else {
            preferenceCategory.removePreference(mCustomCategoryPreference);
        }
    }

    public void scheduleAutoWallpaperJob(SharedPreferences sharedPreferences) {
        boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);

        JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (autoWallpaperEnabled) {
            boolean deviceOnWifiCondition = sharedPreferences.getBoolean("auto_wallpaper_on_wifi", true);
            boolean deviceChargingCondition = sharedPreferences.getBoolean("auto_wallpaper_charging", true);
            boolean deviceIdleCondition = sharedPreferences.getBoolean("auto_wallpaper_idle", true);
            String changeWallpaperInterval = sharedPreferences.getString("auto_wallpaper_interval", getString(R.string.auto_wallpaper_interval_default));
            long changeWallpaperIntervalMillis = TimeUnit.MINUTES.toMillis(Long.valueOf(changeWallpaperInterval));

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
            builder.setPersisted(true);

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
        } else {
            if (jobScheduler != null) {
                jobScheduler.cancelAll();
            }
        }
    }

    public interface OnAutoWallpaperFragmentListener {
        void onAutoWallpaperEnableClicked(boolean enabled);
    }
}
