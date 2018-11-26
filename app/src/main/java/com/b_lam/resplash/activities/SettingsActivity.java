package com.b_lam.resplash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.b_lam.resplash.util.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_settings) Toolbar toolbar;

    private final static String TAG = "SettingsActivity";
    private static boolean settingChanged = false;
    private static boolean activityRestarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.SettingsActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.SettingsActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.main_settings));

        getFragmentManager().beginTransaction().replace(R.id.pref_content, new SettingsFragment()).commit();

        if (!activityRestarted) {
            settingChanged = false;
        }

        activityRestarted = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                if (settingChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (settingChanged) {
            NavUtils.navigateUpFromSameTask(this);
        } else {
            super.onBackPressed();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private FirebaseAnalytics mFirebaseAnalytics;

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            final Preference btnClearCache = findPreference(getString(R.string.title_clear_cache));

            btnClearCache.setSummary(getString(R.string.cache_size) + ": " + dirSize(Glide.getPhotoCacheDir(Resplash.getInstance())) + " MB");
            Log.d(TAG, getString(R.string.cache_size) + ": " + dirSize(Glide.getPhotoCacheDir(Resplash.getInstance())));

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getActivity());
            mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_CLEAR_CACHE, null);

            btnClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AsyncTask<Void, Void, Void>() {
                        protected void onPreExecute() {
                        }
                        protected Void doInBackground(Void... unused) {
                            Glide.get(Resplash.getInstance()).clearDiskCache();
                            return null;
                        }
                        protected void onPostExecute(Void unused) {
                            btnClearCache.setSummary(getString(R.string.cache_size) + ": " + dirSize(Glide.getPhotoCacheDir(Resplash.getInstance())) + " MB");
                            Toast.makeText(Resplash.getInstance(), getString(R.string.message_cache_cleared), Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                    return true;
                }
            });
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
            settingChanged = true;
            LocaleUtils.loadLocale(getActivity().getBaseContext());

            if(key.equals("language")) {
                showRestartSnackbar();
                LocaleUtils.loadLocale(getActivity().getBaseContext());
            }

            if(key.equals("theme")) {
                restartActivity();
            }
        }

        private void showRestartSnackbar() {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.restart_to_apply), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.restart), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restartActivity();
                        }
                    });
            snackbar.getView().setBackgroundColor(ThemeUtils.getThemeAttrColor(getActivity(), R.attr.colorPrimaryDark));
            snackbar.getView().setElevation(Utils.dpToPx(getActivity(), 6));
            snackbar.show();
        }

        private void restartActivity(){
            Intent intent = getActivity().getIntent();
            getActivity().finish();
            startActivity(intent);
            activityRestarted = true;
        }
    }

    private static long dirSize(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File aFileList : fileList) {
                if (aFileList.isDirectory()) {
                    result += dirSize(aFileList);
                } else {
                    result += aFileList.length();
                }
            }
            return result / 1024 / 1024;
        }
        return 0;
    }
}
