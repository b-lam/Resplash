package com.b_lam.resplash.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.b_lam.resplash.R;
import com.b_lam.resplash.fragments.SearchCollectionFragment;
import com.b_lam.resplash.fragments.SearchPhotoFragment;
import com.b_lam.resplash.fragments.SearchUserFragment;
import com.b_lam.resplash.util.ThemeUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends BaseActivity implements EditText.OnEditorActionListener, AdapterView.OnItemSelectedListener{

    private static final int SEARCH_PHOTO_PAGE = 0;
    private static final int SEARCH_COLLECTION_PAGE = 1;
    private static final int SEARCH_USER_PAGE = 2;

    @BindView(R.id.search_editText) EditText mEditText;
    @BindView(R.id.toolbar_search) Toolbar mToolbar;
    @BindView(R.id.search_tabs) TabLayout mTabLayout;
    @BindView(R.id.search_viewpager) ViewPager mViewPager;
    @BindView(R.id.search_options_spinner) Spinner mSpinner;

    private String TAG = "SearchActivity";
    private MenuItem mActionClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mTabLayout.setupWithViewPager(mViewPager);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.search_photos_orientations, R.layout.spinner_row);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

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

        if(!mEditText.getText().toString().isEmpty())
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
        if (!text.isEmpty()) {
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
                if (mEditText.getText().toString().isEmpty()) {
                    mActionClear.setVisible(false);
                } else {
                    mActionClear.setVisible(true);
                }
            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case SEARCH_PHOTO_PAGE:
                    mSpinner.setVisibility(View.VISIBLE);
                    break;
                case SEARCH_COLLECTION_PAGE:
                case SEARCH_USER_PAGE:
                    mSpinner.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String text = mEditText.getText().toString();
        if (!text.isEmpty()) {
            FragmentTransaction transactionPhoto = getSupportFragmentManager().beginTransaction();
            transactionPhoto.replace(R.id.search_photo_container, SearchPhotoFragment.newInstance(text)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getSearchOrientation() {
        switch (mSpinner.getSelectedItemPosition()) {
            case 0:
                return null;
            case 1:
                return "landscape";
            case 2:
                return "portrait";
            case 3:
                return "squarish";
            default:
                return null;
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
