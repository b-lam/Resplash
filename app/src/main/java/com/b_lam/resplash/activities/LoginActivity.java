package com.b_lam.resplash.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.AccessToken;
import com.b_lam.resplash.data.service.AuthorizeService;
import com.b_lam.resplash.data.tools.AuthManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.google.firebase.analytics.FirebaseAnalytics;

import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AuthorizeService.OnRequestAccessTokenListener {

    @BindView(R.id.login_btn) Button btnLogin;
    @BindView(R.id.join_btn) Button btnJoin;
    @BindView(R.id.login_close) ImageButton btnClose;
    @BindView(R.id.activity_login) RelativeLayout relativeLayout;

    private String TAG = "LoginActivity";
    private AuthorizeService mService;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.LoginActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.LoginActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

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
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.join_btn: {
                Uri uri = Uri.parse(Resplash.UNSPLASH_JOIN_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    @Override
    public void onRequestAccessTokenSuccess(Call<AccessToken> call, Response<AccessToken> response) {
        if (response.isSuccessful()) {
            Log.d(TAG, response.body().toString());
            AuthManager.getInstance().writeAccessToken(response.body());
            AuthManager.getInstance().refreshPersonalProfile();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_LOGIN, null);
            startActivity(intent);
        } else {
            Snackbar.make(relativeLayout, getString(R.string.request_token_failed), Snackbar.LENGTH_SHORT).show();
//            setState(NORMAL_STATE);
        }
    }

    @Override
    public void onRequestAccessTokenFailed(Call<AccessToken> call, Throwable t) {
        Log.d(TAG, t.toString());
        Snackbar.make(relativeLayout, getString(R.string.request_token_failed), Snackbar.LENGTH_SHORT).show();
//        setState(NORMAL_STATE);
    }
}
