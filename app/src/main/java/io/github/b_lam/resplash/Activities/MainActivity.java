package io.github.b_lam.resplash.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.Fragments.CollectionFragment;
import io.github.b_lam.resplash.Fragments.FeaturedFragment;
import io.github.b_lam.resplash.Fragments.NewFragment;
import io.github.b_lam.resplash.R;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;

    private String TAG = "MainActivity";
    public Drawer drawer;
    private MenuItem mItemFeaturedLatest, mItemFeaturedOldest, mItemFeaturedPopular, mItemNewLatest, mItemNewOldest, mItemNewPopular, mItemAll, mItemCurated, mItemFeatured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));

        isStoragePermissionGranted();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withToolbar(mToolbar)
                .withHeader(R.layout.nav_header_main)
                .withDelayDrawerClickEvent(200)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Featured").withIdentifier(1).withIcon(getDrawable(R.drawable.ic_whatshot_black_24dp)),
                        new PrimaryDrawerItem().withName("New").withIdentifier(2).withIcon(getDrawable(R.drawable.ic_trending_up_black_24dp)),
                        new PrimaryDrawerItem().withName("Categories").withIdentifier(3).withIcon(getDrawable(R.drawable.ic_collections_black_24dp)),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Settings").withIcon(getDrawable(R.drawable.ic_settings_black_24dp)).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Github").withIcon(getDrawable(R.drawable.github_mark)).withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            switch (position){
                                case 1:
                                    mViewPager.setCurrentItem(0);
                                    break;
                                case 2:
                                    mViewPager.setCurrentItem(1);
                                    break;
                                case 3:
                                    mViewPager.setCurrentItem(2);
                                    break;
                                case 5:
                                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                                    break;
                                case 7:
                                    String url = "https://github.com/b-lam/Resplash";
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.setData(Uri.parse(url));
                                    startActivity(i);
                                    break;
                                default:
                                    break;
                            }
                        }
                        return false;
                    }
                })
                .build();

        drawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(FeaturedFragment.newInstance("latest"), "Featured");
        mPagerAdapter.addFragment(NewFragment.newInstance("latest"), "New");
        mPagerAdapter.addFragment(CollectionFragment.newInstance("Featured"), "Collections");
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if(tab.getPosition() == 0){
                    drawer.setSelection(1);
                }else if(tab.getPosition() == 1){
                    drawer.setSelection(2);
                }else{
                    drawer.setSelection(3);
                }
                Log.d(TAG, String.valueOf(tab.getPosition()));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        mItemFeaturedLatest = menu.findItem(R.id.menu_item_featured_latest);
        mItemFeaturedOldest = menu.findItem(R.id.menu_item_featured_oldest);
        mItemFeaturedPopular = menu.findItem(R.id.menu_item_featured_popular);
        mItemNewLatest = menu.findItem(R.id.menu_item_new_latest);
        mItemNewOldest = menu.findItem(R.id.menu_item_new_oldest);
        mItemNewPopular = menu.findItem(R.id.menu_item_new_popular);
        mItemAll = menu.findItem(R.id.menu_item_all);
        mItemCurated = menu.findItem(R.id.menu_item_curated);
        mItemFeatured = menu.findItem(R.id.menu_item_featured);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        switch (mViewPager.getCurrentItem()){
            case 0:
                mItemFeaturedLatest.setVisible(true);
                mItemFeaturedOldest.setVisible(true);
                mItemFeaturedPopular.setVisible(true);
                mItemNewLatest.setVisible(false);
                mItemNewOldest.setVisible(false);
                mItemNewPopular.setVisible(false);
                mItemAll.setVisible(false);
                mItemCurated.setVisible(false);
                mItemFeatured.setVisible(false);
                break;
            case 1:
                mItemFeaturedLatest.setVisible(false);
                mItemFeaturedOldest.setVisible(false);
                mItemFeaturedPopular.setVisible(false);
                mItemNewLatest.setVisible(true);
                mItemNewOldest.setVisible(true);
                mItemNewPopular.setVisible(true);
                mItemAll.setVisible(false);
                mItemCurated.setVisible(false);
                mItemFeatured.setVisible(false);
                break;
            case 2:
                mItemFeaturedLatest.setVisible(false);
                mItemFeaturedOldest.setVisible(false);
                mItemFeaturedPopular.setVisible(false);
                mItemNewLatest.setVisible(false);
                mItemNewOldest.setVisible(false);
                mItemNewPopular.setVisible(false);
                mItemAll.setVisible(true);
                mItemCurated.setVisible(true);
                mItemFeatured.setVisible(true);
                break;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                return true;
            case R.id.sort_by:
                return true;
            case R.id.menu_item_featured_latest:
                transaction.replace(R.id.featured_container, FeaturedFragment.newInstance("latest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_featured_oldest:
                transaction.replace(R.id.featured_container, FeaturedFragment.newInstance("oldest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_featured_popular:
                transaction.replace(R.id.featured_container, FeaturedFragment.newInstance("popular")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_new_latest:
                transaction.replace(R.id.new_container, NewFragment.newInstance("latest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_new_oldest:
                transaction.replace(R.id.new_container, NewFragment.newInstance("oldest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_new_popular:
                transaction.replace(R.id.new_container, NewFragment.newInstance("popular")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_all:
                transaction.replace(R.id.collection_container, CollectionFragment.newInstance("All")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_curated:
                transaction.replace(R.id.collection_container, CollectionFragment.newInstance("Curated")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            case R.id.menu_item_featured:
                transaction.replace(R.id.collection_container, CollectionFragment.newInstance("Featured")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }

    private void loadPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", mTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mViewPager.setCurrentItem(savedInstanceState.getInt("position"));
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
