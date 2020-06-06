package com.b_lam.resplash.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.tools.AutoWallpaperWorker;
import com.b_lam.resplash.fragments.AutoWallpaperFragment;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.b_lam.resplash.util.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AutoWallpaperActivity extends AppCompatActivity implements AutoWallpaperFragment.OnAutoWallpaperFragmentListener {

    private final static String TAG = "AutoWallpaperActivity";

    @BindView(R.id.auto_wallpaper_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.auto_wallpaper_fab) FloatingActionButton floatingActionButton;
    @BindView(R.id.toolbar_auto_wallpaper) Toolbar toolbar;

    private FirebaseAnalytics firebaseAnalytics;

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

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        boolean enabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("auto_wallpaper", false);
        setFloatingActionButtonVisibility(enabled);

        floatingActionButton.setOnClickListener(view -> setNewWallpaper());
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof AutoWallpaperFragment) {
            AutoWallpaperFragment autoWallpaperFragment = (AutoWallpaperFragment) fragment;
            autoWallpaperFragment.setOnAutoWallpaperFragmentListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_wallpaper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.auto_wallpaper_help:
                showHelpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAutoWallpaperEnableClicked(boolean enabled) {
        setFloatingActionButtonVisibility(enabled);

        if (enabled) {
            firebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_ENABLE_AUTO_WALLPAPER, null);
        }
    }

    private void setNewWallpaper() {
        AutoWallpaperWorker.Companion.scheduleAutoWallpaperJobSingle(this);
        showSnackbar();
    }

    private void setFloatingActionButtonVisibility(boolean visible) {
        if (visible) {
            floatingActionButton.show();
        } else {
            floatingActionButton.hide();
        }
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Not Working?")
                .setMessage("Try disabling the Idle condition and battery optimizations for Resplash in your phone settings")
                .setPositiveButton("Okay", null)
                .create()
                .show();
    }

    private void showSnackbar() {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, getString(R.string.setting_wallpaper), Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimaryDark));
        snackbar.getView().setElevation(Utils.dpToPx(this, 6));

        WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(
                AutoWallpaperWorker.AUTO_WALLPAPER_SINGLE_JOB_ID)
                .observe(this, workInfos -> {
                    if (workInfos != null) {
                        if (workInfos.get(0).getState() == WorkInfo.State.SUCCEEDED ||
                                workInfos.get(0).getState() == WorkInfo.State.FAILED ||
                                workInfos.get(0).getState() == WorkInfo.State.CANCELLED) {
                            snackbar.dismiss();
                        } else if (workInfos.get(0).getState() == WorkInfo.State.RUNNING) {
                            snackbar.show();
                        }
                    }
                });
    }
}
