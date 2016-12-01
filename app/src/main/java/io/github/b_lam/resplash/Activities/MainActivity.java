package io.github.b_lam.resplash.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.fragments.CollectionFragment;
import io.github.b_lam.resplash.fragments.FeaturedFragment;
import io.github.b_lam.resplash.fragments.NewFragment;
import io.github.b_lam.resplash.R;
import io.github.b_lam.resplash.Resplash;

public class MainActivity extends AppCompatActivity{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.tabs) TabLayout mTabLayout;
    @BindView(R.id.fab_upload) FloatingActionButton fabUpload;

    private String TAG = "MainActivity";
    public Drawer drawer = null;
    private AccountHeader drawerHeader = null;
    private MenuItem mItemFeaturedLatest, mItemFeaturedOldest, mItemFeaturedPopular, mItemNewLatest, mItemNewOldest, mItemNewPopular, mItemAll, mItemCurated, mItemFeatured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                if (isFirstStart) {

                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    SharedPreferences.Editor e = getPrefs.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        }).start();

        isStoragePermissionGranted();

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

                return super.placeholder(ctx, tag);
            }
        });

        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        final IProfile profile = new ProfileDrawerItem().withName("Brandon Lam").withEmail("whoisbrandonlam@gmail.com").withIcon("https://images.unsplash.com/profile-1462642403018-51d1fa7ba175?ixlib=rb-0.3.5&q=80&fm=jpg&crop=faces&cs=tinysrgb&fit=crop&h=128&w=128&s=6ed6fa25f925af9332ce3d6b6d2d6d81").withIdentifier(100);

        // Create the AccountHeader
        drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withTextColorRes(R.color.colorText)
                .addProfiles(
                        profile,
                        new ProfileSettingDrawerItem().withName("Add Account").withDescription("Add new Unsplash Account").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_plus).actionBar().paddingDp(5)).withOnDrawerItemClickListener(drawerItemClickListener),
                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(new IconicsDrawable(this, CommunityMaterial.Icon.cmd_settings).paddingDp(4)).withIdentifier(100001).withOnDrawerItemClickListener(drawerItemClickListener)
                )
//                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
//                    @Override
//                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
//                        //sample usage of the onProfileChanged listener
//                        //if the clicked item has the identifier 1 add a new profile ;)
//                        if (profile instanceof IDrawerItem && profile.getIdentifier() == 100000) {
//                            int count = 100 + drawerHeader.getProfiles().size() + 1;
//                            IProfile newProfile = new ProfileDrawerItem().withNameShown(true).withName("Batman" + count).withEmail("batman" + count + "@gmail.com").withIdentifier(count);
//                            if (drawerHeader.getProfiles() != null) {
//                                //we know that there are 2 setting elements. set the new profile above them ;)
//                                drawerHeader.addProfile(newProfile, drawerHeader.getProfiles().size() - 2);
//                            } else {
//                                drawerHeader.addProfiles(newProfile);
//                            }
//                        }
//                        //false if you have not consumed the event and it should close the drawer
//                        return false;
//                    }
//                })
                .withSavedInstance(savedInstanceState)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withToolbar(mToolbar)
                .withDelayDrawerClickEvent(200)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Featured").withIdentifier(1).withIcon(getDrawable(R.drawable.ic_whatshot_black_24dp)),
                        new PrimaryDrawerItem().withName("New").withIdentifier(2).withIcon(getDrawable(R.drawable.ic_trending_up_black_24dp)),
                        new PrimaryDrawerItem().withName("Categories").withIdentifier(3).withIcon(getDrawable(R.drawable.ic_collections_black_24dp)),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Settings").withIdentifier(4).withIcon(getDrawable(R.drawable.ic_settings_black_24dp)).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("About").withIdentifier(5).withIcon(new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_information_outline).sizeDp(24).paddingDp(2)).withSelectable(false),
                        new PrimaryDrawerItem().withName("Login").withIdentifier(6).withIcon(new IconicsDrawable(this).icon(CommunityMaterial.Icon.cmd_plus).sizeDp(24).paddingDp(4)).withSelectable(false)
                )
                .withOnDrawerItemClickListener(drawerItemClickListener)
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

        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(Resplash.UNSPLASH_UPLOAD_URL);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

    }

    private Drawer.OnDrawerItemClickListener drawerItemClickListener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (drawerItem != null) {

                Log.d(TAG, "Identifier: " + drawerItem.getIdentifier());

                Intent intent = null;

                if(drawerItem.getIdentifier() == 1){
                    mViewPager.setCurrentItem(0);
                }else if(drawerItem.getIdentifier() == 2){
                    mViewPager.setCurrentItem(1);
                }else if(drawerItem.getIdentifier() == 3){
                    mViewPager.setCurrentItem(2);
                }else if(drawerItem.getIdentifier() == 4){
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                }else if(drawerItem.getIdentifier() == 5){
                    intent = new Intent(MainActivity.this, AboutActivity.class);
                }else if(drawerItem.getIdentifier() == 6){
                    intent = new Intent(MainActivity.this, LoginActivity.class);
                }else if(drawerItem.getIdentifier() == 100001){
                    intent = new Intent(MainActivity.this, EditProfileActivity.class);
                }

                if (intent != null) {
                    MainActivity.this.startActivity(intent);
                }
            }
            return false;
        }
    };

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
