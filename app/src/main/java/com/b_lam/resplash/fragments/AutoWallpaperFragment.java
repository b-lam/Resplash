package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.b_lam.resplash.R;
import com.b_lam.resplash.activities.WallpaperHistoryActivity;
import com.b_lam.resplash.data.tools.AutoWallpaperWorker;

public class AutoWallpaperFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private CheckBoxPreference mEnableAutoWallpaperPreference;
    private EditTextPreference mCustomCategoryPreference;

    private OnAutoWallpaperFragmentListener mCallback;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.autowallpaperpreferences, rootKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        mEnableAutoWallpaperPreference = findPreference("auto_wallpaper");

        boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);
        enableAutoWallpaper(autoWallpaperEnabled);

        mCustomCategoryPreference = findPreference("auto_wallpaper_custom_category");
        boolean customCategorySelected = sharedPreferences
                .getString("auto_wallpaper_category", getString(R.string.auto_wallpaper_category_default))
                .equals("Custom");
        showCustomCategoryPreference(customCategorySelected);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PreferenceCategory conditionsPreferenceCategory = findPreference("auto_wallpaper_conditions");
            CheckBoxPreference deviceIdlePreference = findPreference("auto_wallpaper_idle");
            conditionsPreferenceCategory.removePreference(deviceIdlePreference);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            PreferenceScreen preferenceScreen = findPreference("auto_wallpaper_preference_screen");
            PreferenceCategory optionsPreferenceCategory = findPreference("auto_wallpaper_options");
            preferenceScreen.removePreference(optionsPreferenceCategory);
        }

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
            AutoWallpaperWorker.Companion.scheduleAutoWallpaperJob(getContext());
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
        PreferenceCategory preferenceCategory = findPreference("auto_wallpaper_source");
        if (show) {
            preferenceCategory.addPreference(mCustomCategoryPreference);
        } else {
            preferenceCategory.removePreference(mCustomCategoryPreference);
        }
    }

    public interface OnAutoWallpaperFragmentListener {
        void onAutoWallpaperEnableClicked(boolean enabled);
    }
}
