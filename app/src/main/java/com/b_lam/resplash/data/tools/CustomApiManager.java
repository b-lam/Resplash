package com.b_lam.resplash.data.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;

/**
 * Custom API manager.
 *
 * This manager class can manage the custom API key and secret.
 *
 * */

public class CustomApiManager {

    private static CustomApiManager instance;

    public static CustomApiManager getInstance(Context context) {
        if (instance == null) {
            instance = new CustomApiManager(context);
        }
        return instance;
    }

    private String customApiKey;
    private String customApiSecret;

    private static final String PREFERENCE_RESPLASH_API_MANAGER = "resplash_api_manager";
    private static final String KEY_CUSTOM_API_KEY = "custom_api_key";
    private static final String KEY_CUSTOM_API_SECRET = "custom_api_secret";

    private CustomApiManager(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_RESPLASH_API_MANAGER, Context.MODE_PRIVATE);
        this.customApiKey = sharedPreferences.getString(KEY_CUSTOM_API_KEY, null);
        this.customApiSecret = sharedPreferences.getString(KEY_CUSTOM_API_SECRET, null);
    }

    public String getCustomApiKey() {
        return customApiKey;
    }

    public String getCustomApiSecret() {
        return customApiSecret;
    }

    public void setCustomApi(Context context, String key, String secret) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_RESPLASH_API_MANAGER, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_CUSTOM_API_KEY, key);
        editor.putString(KEY_CUSTOM_API_SECRET, secret);
        editor.apply();
        this.customApiKey = key;
        this.customApiSecret = secret;
    }
}
