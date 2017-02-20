package com.b_lam.resplash.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        super.onCreate(savedInstanceState);
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
        upArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);

        onRequestUserProfileListener = new UserService.OnRequestUserProfileListener() {
            @Override
            public void onRequestUserProfileSuccess(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    mUser = response.body();
                    tvUserLocation.setText(mUser.location != null ? mUser.location : "Unknown");
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
                    mPagerAdapter.addFragment(userPhotoFragment, mUser.total_photos + " Photos");
                    mPagerAdapter.addFragment(userLikesFragment, mUser.total_likes + " Likes");
                    mPagerAdapter.addFragment(userCollectionFragment, mUser.total_collections + " Collections");

                    mViewPager.setAdapter(mPagerAdapter);
                    mViewPager.setOffscreenPageLimit(2);
                    mTabLayout.setupWithViewPager(mViewPager);

                } else if (response.code() == 403) {
                    Toast.makeText(Resplash.getInstance().getApplicationContext(), "Can't make anymore requests.", Toast.LENGTH_LONG).show();
                } else {
                    mUserService.requestUserProfile(username, this);
                }
            }

            @Override
            public void onRequestUserProfileFailed(Call<User> call, Throwable t) {
                mUserService.requestUserProfile(username, this);
            }
        };

        mUserService.requestUserProfile(username, onRequestUserProfileListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                supportFinishAfterTransition();
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
