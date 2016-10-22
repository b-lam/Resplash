package io.github.b_lam.resplash.Activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.R;
import io.github.b_lam.resplash.Resplash;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.login_btn) Button btnLogin;
    @BindView(R.id.join_btn) Button btnJoin;
    @BindView(R.id.login_close) ImageButton btnClose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        btnLogin.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        btnClose.setOnClickListener(this);
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
}
