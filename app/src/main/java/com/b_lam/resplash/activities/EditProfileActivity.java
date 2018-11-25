package com.b_lam.resplash.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.b_lam.resplash.data.data.Me;
import com.b_lam.resplash.data.service.UserService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity implements UserService.OnRequestMeProfileListener, AuthManager.OnAuthDataChangedListener {

    @BindView(R.id.toolbar_edit_profile) Toolbar mToolbar;
    @BindView(R.id.username_edit_text) EditText mUsername;
    @BindView(R.id.first_name_edit_text) EditText mFirstName;
    @BindView(R.id.last_name_edit_text) EditText mLastName;
    @BindView(R.id.email_edit_text) EditText mEmail;
    @BindView(R.id.portfolio_edit_text) EditText mPortfolio;
    @BindView(R.id.instagram_edit_text) EditText mInstagram;
    @BindView(R.id.location_edit_text) EditText mLocation;
    @BindView(R.id.bio_edit_text) EditText mBio;
    @BindView(R.id.save_edit_button) Button mSave;

    private UserService mService;

    private boolean backPressed = false;

    private final String KEY_UPDATE_PROFILE_USERNAME = "update_profile_username";
    private final String KEY_UPDATE_PROFILE_FIRSTNAME = "update_profile_firstname";
    private final String KEY_UPDATE_PROFILE_LASTNAME = "update_profile_lastname";
    private final String KEY_UPDATE_PROFILE_EMAIL = "update_profile_email";
    private final String KEY_UPDATE_PROFILE_PORTFOLIO = "update_profile_portfolio";
    private final String KEY_UPDATE_PROFILE_INSTAGRAM = "update_profile_instagram";
    private final String KEY_UPDATE_PROFILE_LOCATION = "update_profile_location";
    private final String KEY_UPDATE_PROFILE_BIO = "update_profile_bio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.EditProfileActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.EditProfileActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_edit_profile);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.edit_title));

        this.mService = UserService.getService();

        mService.requestMeProfile(new UserService.OnRequestMeProfileListener() {
            @Override
            public void onRequestMeProfileSuccess(Call<Me> call, Response<Me> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthManager.getInstance().writeUserInfo(response.body());
                } else {
                    Toast.makeText(EditProfileActivity.this, getString(R.string.failed_to_load_profile), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestMeProfileFailed(Call<Me> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, getString(R.string.failed_to_load_profile), Toast.LENGTH_SHORT).show();
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        AuthManager.getInstance().addOnWriteDataListener(this);
        if (AuthManager.getInstance().isAuthorized() && TextUtils.isEmpty(AuthManager.getInstance().getUsername())) {
            AuthManager.getInstance().refreshPersonalProfile();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mService.cancel();
        AuthManager.getInstance().removeOnWriteDataListener(this);
        AuthManager.getInstance().cancelRequest();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_UPDATE_PROFILE_USERNAME, mUsername.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_FIRSTNAME, mFirstName.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_LASTNAME, mLastName.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_EMAIL, mEmail.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_PORTFOLIO, mPortfolio.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_INSTAGRAM, mInstagram.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_LOCATION, mLocation.getText().toString());
        outState.putString(KEY_UPDATE_PROFILE_BIO, mBio.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadData(){
        mUsername.setText(AuthManager.getInstance().getMe().username);
        mFirstName.setText(AuthManager.getInstance().getMe().first_name);
        mLastName.setText(AuthManager.getInstance().getMe().last_name);
        mEmail.setText(AuthManager.getInstance().getMe().email);
        mPortfolio.setText(AuthManager.getInstance().getMe().portfolio_url);
        mInstagram.setText(AuthManager.getInstance().getMe().instagram_username);
        mLocation.setText(AuthManager.getInstance().getMe().location);
        mBio.setText(AuthManager.getInstance().getMe().bio);
    }

    private void updateProfile(){

        String username = mUsername.getText().toString();
        if (!TextUtils.isEmpty(username)) {
            mService.updateMeProfile(
                    username,
                    mFirstName.getText().toString(),
                    mLastName.getText().toString(),
                    mEmail.getText().toString(),
                    mPortfolio.getText().toString(),
                    mLocation.getText().toString(),
                    mBio.getText().toString(),
                    this);
        } else {
            Toast.makeText(this, getString(R.string.username_cannot_be_blank), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestMeProfileSuccess(Call<Me> call, Response<Me> response) {
        if (response.isSuccessful() && response.body() != null) {
            AuthManager.getInstance().writeUserInfo(response.body());
            Toast.makeText(this, getString(R.string.updated_profile), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.cannot_update_profile), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestMeProfileFailed(Call<Me> call, Throwable t) {
        Toast.makeText(this, getString(R.string.cannot_update_profile), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWriteAccessToken() {
    }

    @Override
    public void onWriteUserInfo() {
        loadData();
    }

    @Override
    public void onWriteAvatarPath() {
    }

    @Override
    public void onLogout() {
    }
}
