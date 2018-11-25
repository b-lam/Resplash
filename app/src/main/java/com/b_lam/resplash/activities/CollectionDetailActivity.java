package com.b_lam.resplash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.CircleImageView;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.service.PhotoService;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;
import tr.xip.errorview.ErrorView;

public class CollectionDetailActivity extends AppCompatActivity {

    @BindView(R.id.fragment_collection_detail_recycler) RecyclerView mImageRecycler;
    @BindView(R.id.swipeContainerCollectionDetail) SwipeRefreshLayout mSwipeContainer;
    @BindView(R.id.fragment_collection_detail_progress) ProgressBar mImagesProgress;
    @BindView(R.id.fragment_collection_detail_error_view) ErrorView mImagesErrorView;
    @BindView(R.id.toolbar_collection_detail) Toolbar mToolbar;
    @BindView(R.id.tvCollectionDescription) TextView mCollectionDescription;
    @BindView(R.id.tvUserCollection) TextView mUserCollection;
    @BindView(R.id.imgProfileCollection)
    CircleImageView mUserProfilePicture;

    private String TAG = "CollectionDetails";
    private Collection mCollection;
    private FastItemAdapter<Photo> mPhotoAdapter;
    private List<Photo> mPhotos;
    private List<Photo> mCurrentPhotos;
    private ItemAdapter mFooterAdapter;
    private int mPage, mColumns;
    private PhotoService.OnRequestPhotosListener mPhotosRequestListener;
    private String mLayoutType;
    private PhotoService photoService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.CollectionDetailActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.CollectionDetailActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_collection_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollection = new Gson().fromJson(getIntent().getStringExtra("Collection"), Collection.class);

        this.photoService = PhotoService.getService();

        setTitle(mCollection.title);
        if (mCollection.description != null){
            mCollectionDescription.setText(mCollection.description);
            mCollectionDescription.setVisibility(View.VISIBLE);
        }else{
            mCollectionDescription.setVisibility(View.GONE);
        }
        mUserCollection.setText(getString(R.string.by_author, mCollection.user.name));
        Glide.with(CollectionDetailActivity.this).load(mCollection.user.profile_image.medium).into(mUserProfilePicture);

        mUserProfilePicture.setOnClickListener(userProfileOnClickListener);
        mUserCollection.setOnClickListener(userProfileOnClickListener);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        mLayoutType = sharedPreferences.getString("item_layout", "List");
        mPage = 1;
        if(mLayoutType.equals("List") || mLayoutType.equals("Cards")){
            mColumns = 1;
        }else{
            mColumns = 2;
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, mColumns);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        mPhotoAdapter = new FastItemAdapter<>();

        mPhotoAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter<>();

        mPhotoAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mPhotoAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                if(mPhotoAdapter.getItemCount() >= mCollection.total_photos){
                    Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.no_more_photos), Toast.LENGTH_LONG);
                }else {
                    mFooterAdapter.clear();
                    mFooterAdapter.add(new ProgressItem().withEnabled(false));
                    loadMore();
                }
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMore();
            }
        });

        loadMore();
    }

    private OnClickListener<Photo> onClickListener = new OnClickListener<Photo>(){
        @Override
        public boolean onClick(View v, IAdapter<Photo> adapter, Photo item, int position) {
            Intent i = new Intent(getApplicationContext(), DetailActivity.class);
            i.putExtra("Photo", new Gson().toJson(item));
            String layout = sharedPreferences.getString("item_layout", "List");

            ImageView imageView;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || sharedPreferences.getString("item_layout", "List").equals("Grid")) {
                startActivity(i);
            } else if (layout.equals("Cards")) {
                imageView = (ImageView) v.findViewById(R.id.item_image_card_img);
                if (imageView.getDrawable() != null)
                    Resplash.getInstance().setDrawable(imageView.getDrawable());
                startActivity(i);
            } else {
                imageView = (ImageView) v.findViewById(R.id.item_image_img);
                if (imageView.getDrawable() != null)
                    Resplash.getInstance().setDrawable(imageView.getDrawable());
//                v.setTransitionName("photoScale");
//                Pair<View, String> p1 = Pair.create(v, v.getTransitionName());
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(CollectionDetailActivity.this, p1);
//                startActivity(i, options.toBundle());
                startActivity(i);
            }
            return false;
        }
    };

    public void updateAdapter(List<Photo> photos) {
        mCurrentPhotos = photos;
        mPhotoAdapter.add(mCurrentPhotos);
    }

    public void loadMore(){
        if(mPhotos == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }


        mPhotosRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mPhotos = response.body();
                    mFooterAdapter.clear();
                    CollectionDetailActivity.this.updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                }else{
                    mImagesErrorView.setTitle(R.string.error_http);
                    mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                }
                if(mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.updated_photos), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.setRetryVisible(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mCollection.curated){
            photoService.requestCuratedCollectionPhotos(mCollection, mPage, Resplash.DEFAULT_PER_PAGE, mPhotosRequestListener);
        }else{
            photoService.requestCollectionPhotos(mCollection, mPage, Resplash.DEFAULT_PER_PAGE, mPhotosRequestListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:
                shareTextUrl();
                return true;
            case R.id.action_view_on_unsplash:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mCollection.links.html + Resplash.UNSPLASH_UTM_PARAMETERS));
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivity(intent);
                else
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (photoService != null) {
            photoService.cancel();
        }
    }

    private View.OnClickListener userProfileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            intent.putExtra("username", mCollection.user.username);
            intent.putExtra("name", mCollection.user.name);
            startActivity(intent);
        }
    };

    private void shareTextUrl() {
        if(mCollection != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unsplash_collection));
            share.putExtra(Intent.EXTRA_TEXT, mCollection.links.html + Resplash.UNSPLASH_UTM_PARAMETERS);

            startActivity(Intent.createChooser(share, getString(R.string.share_via)));
        }
    }
}
