package io.github.b_lam.resplash.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.data.data.AccessToken;
import io.github.b_lam.resplash.data.service.AuthorizeService;
import io.github.b_lam.resplash.data.tools.AuthManager;
import io.github.b_lam.resplash.R;
import io.github.b_lam.resplash.Resplash;
import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AuthorizeService.OnRequestAccessTokenListener {

    @BindView(R.id.login_btn) Button btnLogin;
    @BindView(R.id.join_btn) Button btnJoin;
    @BindView(R.id.login_close) ImageButton btnClose;

    private String TAG = "LoginActivity";
    private AuthorizeService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        btnLogin.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        mService = AuthorizeService.getService();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null
                && intent.getData() != null
                && !TextUtils.isEmpty(intent.getData().getAuthority())
                && Resplash.UNSPLASH_LOGIN_CALLBACK.equals(intent.getData().getAuthority())) {
            mService.requestAccessToken(intent.getData().getQueryParameter("code"), this);
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
                Uri uri = Uri.parse(Resplash.UNSPLASH_LOGIN_URL);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                break;
            }

            case R.id.join_btn: {
                Uri uri = Uri.parse(Resplash.UNSPLASH_JOIN_URL);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
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
            finish();
        } else {
//            NotificationUtils.showSnackbar(
//                    getString(R.string.feedback_request_token_failed),
//                    Snackbar.LENGTH_SHORT);
//            setState(NORMAL_STATE);
        }
    }

    @Override
    public void onRequestAccessTokenFailed(Call<AccessToken> call, Throwable t) {
        Log.d(TAG, t.toString());
//        NotificationUtils.showSnackbar(
//                getString(R.string.feedback_request_token_failed),
//                Snackbar.LENGTH_SHORT);
//        setState(NORMAL_STATE);
    }
}
