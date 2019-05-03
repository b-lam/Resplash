package com.b_lam.resplash.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.browser.customtabs.CustomTabsIntent;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.model.AccessToken;
import com.b_lam.resplash.data.service.AuthorizeService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.helpers.customtabs.CustomTabsHelper;
import com.b_lam.resplash.util.ThemeUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener, AuthorizeService.OnRequestAccessTokenListener {

    @BindView(R.id.login_btn) Button btnLogin;
    @BindView(R.id.join_btn) Button btnJoin;
    @BindView(R.id.login_close) ImageButton btnClose;
    @BindView(R.id.activity_login) RelativeLayout relativeLayout;

    public static final int LOGIN_ACTIVITY_RESULT_CODE = 892;

    private String TAG = "LoginActivity";
    private AuthorizeService mService;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        btnLogin.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        mService = AuthorizeService.getService();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null
                && intent.getData() != null
                && !TextUtils.isEmpty(intent.getData().getAuthority())
                && Resplash.UNSPLASH_LOGIN_CALLBACK.equals(intent.getData().getAuthority())) {
            mService.requestAccessToken(this, intent.getData().getQueryParameter("code"), this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mService.cancel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_close:
                finish();
                break;

            case R.id.login_btn: {
                Uri uri = Uri.parse(Resplash.getLoginUrl(this));
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                        .setToolbarColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary))
                        .setShowTitle(true)
                        .build();
                CustomTabsHelper.Companion.openCustomTab(this, customTabsIntent, uri);
                break;
            }

            case R.id.join_btn: {
                Uri uri = Uri.parse(Resplash.UNSPLASH_JOIN_URL);
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                        .setToolbarColor(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary))
                        .setShowTitle(true)
                        .build();
                CustomTabsHelper.Companion.openCustomTab(this, customTabsIntent, uri);
                break;
            }
        }
    }

    @Override
    public void onRequestAccessTokenSuccess(Call<AccessToken> call, Response<AccessToken> response) {
        if (response.isSuccessful()) {
            AuthManager.getInstance().writeAccessToken(response.body());
            AuthManager.getInstance().requestPersonalProfile();
            mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_LOGIN, null);
            setResult(RESULT_OK);
            finish();
        } else {
            Snackbar.make(relativeLayout, getString(R.string.request_token_failed), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestAccessTokenFailed(Call<AccessToken> call, Throwable t) {
        Log.d(TAG, t.toString());
        Snackbar.make(relativeLayout, getString(R.string.request_token_failed), Snackbar.LENGTH_SHORT).show();
    }
}
