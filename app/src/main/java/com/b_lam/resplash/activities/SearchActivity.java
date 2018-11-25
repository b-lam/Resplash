package com.b_lam.resplash.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.b_lam.resplash.fragments.SearchCollectionFragment;
import com.b_lam.resplash.fragments.SearchPhotoFragment;
import com.b_lam.resplash.fragments.SearchUserFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;

public class SearchActivity extends AppCompatActivity implements EditText.OnEditorActionListener{

    @BindView(R.id.search_editText) EditText mEditText;
    @BindView(R.id.toolbar_search) Toolbar mToolbar;
    @BindView(R.id.search_tabs) TabLayout mTabLayout;
    @BindView(R.id.search_viewpager) ViewPager mViewPager;

    private String TAG = "SearchActivity";
    private MenuItem mActionClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.SearchActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.SearchActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(SearchPhotoFragment.newInstance(null), getString(R.string.search_photos));
        mPagerAdapter.addFragment(SearchCollectionFragment.newInstance(null), getString(R.string.search_collections));
        mPagerAdapter.addFragment(SearchUserFragment.newInstance(null), getString(R.string.search_users));
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mViewPager);

        mEditText.setOnEditorActionListener(this);
        mEditText.setFocusable(true);
        mEditText.requestFocus();
        mEditText.addTextChangedListener(textWatcher);

        if(mEditText.requestFocus() && mEditText.getText().toString().equals("")) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(!mEditText.getText().toString().equals("")) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        mActionClear = menu.getItem(0);

        if(!mEditText.getText().toString().equals(""))
            mActionClear.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_clear_text:
                mEditText.setText(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        FragmentTransaction transactionPhoto = getSupportFragmentManager().beginTransaction();
        FragmentTransaction transactionCollection = getSupportFragmentManager().beginTransaction();
        FragmentTransaction transactionUser = getSupportFragmentManager().beginTransaction();

        String text = textView.getText().toString();
        if (!text.equals("")) {
            transactionPhoto.replace(R.id.search_photo_container, SearchPhotoFragment.newInstance(text)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            transactionCollection.replace(R.id.search_collection_container, SearchCollectionFragment.newInstance(text)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
            transactionUser.replace(R.id.search_user_container, SearchUserFragment.newInstance(text)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        return true;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(mActionClear != null) {
                if (mEditText.getText().toString().equals("")) {
                    mActionClear.setVisible(false);
                } else {
                    mActionClear.setVisible(true);
                }
            }
        }
    };

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
