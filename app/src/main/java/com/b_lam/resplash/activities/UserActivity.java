package com.b_lam.resplash.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.User;
import com.b_lam.resplash.data.service.UserService;
import com.b_lam.resplash.fragments.UserCollectionFragment;
import com.b_lam.resplash.fragments.UserLikesFragment;
import com.b_lam.resplash.fragments.UserPhotoFragment;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    @BindView(R.id.user_toolbar) Toolbar mToolbar;
    @BindView(R.id.user_profile_picture) ImageView profilePicture;
    @BindView(R.id.tvUserLocation) TextView tvUserLocation;
    @BindView(R.id.tvUserPortfolioUrl) TextView tvUserPortfolioUrl;
    @BindView(R.id.tvUserBio) TextView tvUserBio;
    @BindView(R.id.user_links_progress) ProgressBar linkProgress;
    @BindView(R.id.user_link_container) LinearLayout linkContainer;
    @BindView(R.id.user_tabs) TabLayout mTabLayout;
    @BindView(R.id.user_viewpager) ViewPager mViewPager;

    private UserService mUserService;
    private UserService.OnRequestUserProfileListener onRequestUserProfileListener;
    private User mUser;
    private String username, name;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.UserActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.UserActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);

        username = getIntent().getStringExtra("username");
        name = getIntent().getStringExtra("name");

        if(Resplash.getInstance().getDrawable() != null){
            profilePicture.setImageDrawable(Resplash.getInstance().getDrawable());
            Resplash.getInstance().setDrawable(null);
        }

        this.mUserService = UserService.getService();

        setSupportActionBar(mToolbar);
        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        onRequestUserProfileListener = new UserService.OnRequestUserProfileListener() {
            @Override
            public void onRequestUserProfileSuccess(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    mUser = response.body();
                    tvUserLocation.setText(mUser.location != null ? mUser.location : getString(R.string.unknown));
                    if(mUser.portfolio_url != null) {
                        tvUserPortfolioUrl.setText(mUser.portfolio_url);
                        tvUserPortfolioUrl.setVisibility(View.VISIBLE);
                    }else{
                        tvUserPortfolioUrl.setVisibility(View.GONE);
                    }
                    if(mUser.bio != null){
                        tvUserBio.setText(mUser.bio);
                        tvUserBio.setVisibility(View.VISIBLE);
                    }else{
                        tvUserBio.setVisibility(View.GONE);
                    }
                    if(Resplash.getInstance().getDrawable() == null){
                        Glide.with(getApplicationContext()).load(mUser.profile_image.large).into(profilePicture);
                    }
                    linkContainer.setVisibility(View.VISIBLE);
                    linkProgress.setVisibility(View.GONE);

                    //Load fragments

                    UserPhotoFragment userPhotoFragment = UserPhotoFragment.newInstance("latest");
                    userPhotoFragment.setUser(mUser);

                    UserLikesFragment userLikesFragment = UserLikesFragment.newInstance("latest");
                    userLikesFragment.setUser(mUser);

                    UserCollectionFragment userCollectionFragment = UserCollectionFragment.newInstance();
                    userCollectionFragment.setUser(mUser);

                    mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
                    mPagerAdapter.addFragment(userPhotoFragment, getString(R.string.photos, String.valueOf(mUser.total_photos)));
                    mPagerAdapter.addFragment(userLikesFragment, getString(R.string.likes, String.valueOf(mUser.total_likes)));
                    mPagerAdapter.addFragment(userCollectionFragment, mUser.total_collections + " " + getString(R.string.main_collections));

                    mViewPager.setAdapter(mPagerAdapter);
                    mViewPager.setOffscreenPageLimit(2);
                    mTabLayout.setupWithViewPager(mViewPager);

                } else if (response.code() == 403) {
                    Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.cannot_make_anymore_requests), Toast.LENGTH_LONG).show();
                } else {
                    if(username != null) {
                        mUserService.requestUserProfile(username, this);
                    }
                }
            }

            @Override
            public void onRequestUserProfileFailed(Call<User> call, Throwable t) {
                if(username != null) {
                    mUserService.requestUserProfile(username, this);
                }
            }
        };

        if(username != null) {
            mUserService.requestUserProfile(username, onRequestUserProfileListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_view_on_unsplash:
                if (mUser != null && mUser.links != null && mUser.links.html != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUser.links.html + Resplash.UNSPLASH_UTM_PARAMETERS));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUserService != null) {
            mUserService.cancel();
        }
    }

    class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position){
            return fragmentTitleList.get(position);
        }
    }
}
