package com.b_lam.resplash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.service.PhotoService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.dialogs.EditCollectionDialog;
import com.b_lam.resplash.util.ThemeUtils;
import com.b_lam.resplash.views.CircleImageView;
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
import retrofit2.Call;
import retrofit2.Response;

public class CollectionDetailActivity extends BaseActivity implements EditCollectionDialog.EditCollectionDialogListener {

    @BindView(R.id.fragment_collection_detail_recycler) RecyclerView mImageRecycler;
    @BindView(R.id.swipeContainerCollectionDetail) SwipeRefreshLayout mSwipeContainer;
    @BindView(R.id.fragment_collection_detail_progress) ProgressBar mImagesProgress;
    @BindView(R.id.http_error_view) ConstraintLayout mHttpErrorView;
    @BindView(R.id.network_error_view) ConstraintLayout mNetworkErrorView;
    @BindView(R.id.no_results_view) ConstraintLayout mNoResultsView;
    @BindView(R.id.toolbar_collection_detail) Toolbar mToolbar;
    @BindView(R.id.tvCollectionDescription) TextView mCollectionDescription;
    @BindView(R.id.tvUserCollection) TextView mUserCollection;
    @BindView(R.id.imgProfileCollection) CircleImageView mUserProfilePicture;

    public final static String USER_COLLECTION_FLAG = "USER_COLLECTION_FLAG";
    public final static String COLLECTION_DETAIL_ID_FLAG = "COLLECTION_DETAIL_ID_FLAG";
    public final static String PHOTO_REMOVED_FLAG = "PHOTO_REMOVED_FLAG";
    public final static int COLLECTION_DETAIL_REQUEST_FLAG = 24503;

    private final static String TAG = "CollectionDetails";
    private Collection mCollection;
    private FastItemAdapter<Photo> mPhotoAdapter;
    private List<Photo> mPhotos;
    private ItemAdapter mFooterAdapter;
    private int mPage;
    private PhotoService photoService;
    private SharedPreferences sharedPreferences;
    private MenuItem mEditButton;
    private boolean mIsUserCollection = false;
    private int mClickedPhotoPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_collection_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollection = new Gson().fromJson(getIntent().getStringExtra("Collection"), Collection.class);
        mIsUserCollection = getIntent().getBooleanExtra(USER_COLLECTION_FLAG, false);

        this.photoService = PhotoService.getService();

        setCollection(mCollection);

        Glide.with(getApplicationContext()).load(mCollection.user.profile_image.medium).into(mUserProfilePicture);

        mUserProfilePicture.setOnClickListener(userProfileOnClickListener);
        mUserCollection.setOnClickListener(userProfileOnClickListener);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());

        String layoutType = sharedPreferences.getString("item_layout", "List");
        int columns = layoutType.equals("List") || layoutType.equals("Cards") ? 1 : 2;

        mPage = 1;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columns);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener((v, event) -> false);

        mPhotoAdapter = new FastItemAdapter<>();

        mPhotoAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter<>();

        mPhotoAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mPhotoAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                if (mPhotoAdapter.getItemCount() >= mCollection.total_photos && mPage > 2) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_more_photos), Toast.LENGTH_LONG).show();
                } else {
                    mFooterAdapter.clear();
                    mFooterAdapter.add(new ProgressItem().withEnabled(false));
                    loadMore();
                }
            }
        });

        mSwipeContainer.setOnRefreshListener(() -> {
            mPage = 1;
            loadMore();
        });

        loadMore();
    }

    private OnClickListener<Photo> onClickListener = new OnClickListener<Photo>(){
        @Override
        public boolean onClick(View v, IAdapter<Photo> adapter, Photo item, int position) {
            mClickedPhotoPosition = position;
            Intent i = new Intent(getApplicationContext(), DetailActivity.class);
            i.putExtra("Photo", new Gson().toJson(item));
            i.putExtra(COLLECTION_DETAIL_ID_FLAG, mCollection.id);
            String layout = sharedPreferences.getString("item_layout", "List");

            if (layout.equals("Cards")) {
                ImageView imageView = v.findViewById(R.id.item_image_card_img);
                if (imageView.getDrawable() != null) Resplash.getInstance().setDrawable(imageView.getDrawable());
            } else if (layout.equals("List")){
                ImageView imageView = v.findViewById(R.id.item_image_img);
                if (imageView.getDrawable() != null) Resplash.getInstance().setDrawable(imageView.getDrawable());
            }

            if (mIsUserCollection) {
                startActivityForResult(i, COLLECTION_DETAIL_REQUEST_FLAG);
            } else {
                startActivity(i);
            }

            return false;
        }
    };

    private void setCollection(Collection collection) {
        setTitle(collection.title);

        if (collection.description != null && !collection.description.isEmpty()) {
            mCollectionDescription.setText(collection.description);
            mCollectionDescription.setVisibility(View.VISIBLE);
        } else {
            mCollectionDescription.setVisibility(View.GONE);
        }

        mUserCollection.setText(getString(R.string.by_author, collection.user.name));
    }

    public void updateAdapter(List<Photo> photos) {
        mPhotoAdapter.add(photos);
    }

    public void loadMore(){
        if (mPhotos == null) {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        PhotoService.OnRequestPhotosListener photosRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (response.code() == 200) {
                    mPhotos = response.body();
                    mFooterAdapter.clear();
                    updateAdapter(mPhotos);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                } else {
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
                if (mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.updated_photos), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if (mCollection.total_photos == 0) {
            mNoResultsView.setVisibility(View.VISIBLE);
            mSwipeContainer.setEnabled(false);
        } else if (mCollection.curated) {
            photoService.requestCuratedCollectionPhotos(mCollection, mPage, Resplash.DEFAULT_PER_PAGE, photosRequestListener);
        } else {
            photoService.requestCollectionPhotos(mCollection, mPage, Resplash.DEFAULT_PER_PAGE, photosRequestListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == COLLECTION_DETAIL_REQUEST_FLAG) {
                if (data.getBooleanExtra(PHOTO_REMOVED_FLAG, false)) {
                    mPhotoAdapter.remove(mClickedPhotoPosition);
                    if (mPhotoAdapter.getAdapterItemCount() == 0) {
                        mNoResultsView.setVisibility(View.VISIBLE);
                        mSwipeContainer.setEnabled(false);
                    }
                }
            }
            setResult(RESULT_OK);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.collection, menu);
        mEditButton = menu.findItem(R.id.action_edit);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mEditButton.setVisible(mIsUserCollection);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
            case R.id.action_edit:
                if (AuthManager.getInstance().isAuthorized()) {
                    EditCollectionDialog editCollectionDialog = new EditCollectionDialog();
                    editCollectionDialog.show(getSupportFragmentManager(), null);
                    editCollectionDialog.setListener(this);
                    editCollectionDialog.setCollection(mCollection);
                } else {
                    Toast.makeText(this, getString(R.string.need_to_log_in), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, LoginActivity.class));
                }
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
        if (mCollection != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unsplash_collection));
            share.putExtra(Intent.EXTRA_TEXT, mCollection.links.html + Resplash.UNSPLASH_UTM_PARAMETERS);

            startActivity(Intent.createChooser(share, getString(R.string.share_via)));
        }
    }

    @Override
    public void onCollectionUpdated(Collection collection) {
        mCollection = collection;
        setCollection(mCollection);
        setResult(RESULT_OK);
    }

    @Override
    public void onCollectionDeleted() {
        setResult(RESULT_OK);
        finish();
    }
}
