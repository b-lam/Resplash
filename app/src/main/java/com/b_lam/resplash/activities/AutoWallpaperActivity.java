package com.b_lam.resplash.activities;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.b_lam.resplash.R;
import com.b_lam.resplash.util.ThemeUtils;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

public class AutoWallpaperActivity extends BaseActivity {

    @BindView(R.id.toolbar_auto_wallpaper) Toolbar toolbar;

    private final static String TAG = "AutoWallpaperActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.LIGHT:
                setTheme(R.style.SettingsActivityThemeLight);
                break;
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.SettingsActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.SettingsActivityThemeBlack);
                break;
        }

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

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.autowallpaperpreferences, rootKey);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            boolean autoWallpaperEnabled = sharedPreferences.getBoolean("auto_wallpaper", false);
            enableAutoWallpaper(autoWallpaperEnabled);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("auto_wallpaper")) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("auto_wallpaper");
                enableAutoWallpaper(checkBoxPreference.isChecked());
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

        }

        private void enableAutoWallpaper(boolean enable) {
            CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference("auto_wallpaper");
            if (enable) {
                checkBoxPreference.setTitle(R.string.on);
            } else {
                checkBoxPreference.setTitle(R.string.off);
            }
        }
    }
}
