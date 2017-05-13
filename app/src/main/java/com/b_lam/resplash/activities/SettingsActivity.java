package com.b_lam.resplash.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar_settings) Toolbar toolbar;

    private final static String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.pref_content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public static class SettingsFragment extends PreferenceFragment {

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
