package com.b_lam.resplash.activities;

import android.os.Bundle;

import com.b_lam.resplash.R;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.AboutActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.AboutActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);
    }
}
