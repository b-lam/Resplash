package com.b_lam.resplash.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.b_lam.resplash.BuildConfig;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.model.LikePhotoResult;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.service.PhotoService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.dialogs.InfoDialog;
import com.b_lam.resplash.dialogs.ManageCollectionsDialog;
import com.b_lam.resplash.dialogs.StatsDialog;
import com.b_lam.resplash.dialogs.WallpaperDialog;
import com.b_lam.resplash.fragments.UserLikesFragment;
import com.b_lam.resplash.helpers.DownloadHelper;
import com.b_lam.resplash.util.ThemeUtils;
import com.b_lam.resplash.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType;
import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType.DOWNLOAD;
import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType.WALLPAPER;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DetailActivity extends BaseActivity implements ManageCollectionsDialog.ManageCollectionsDialogListener {

    public static final String DETAIL_ACTIVITY_PHOTO_ID_KEY = "DETAIL_ACTIVITY_PHOTO_ID_KEY";

    private static final String TAG = "DetailActivity";
    private boolean mPhotoLike = false;
    private boolean mInCollection = false;
    private boolean mLoadPhotoFromId = false;
    private int mComingFromCollectionId;
    private Photo mPhoto;
    private PhotoService mService;
    private SharedPreferences sharedPreferences;
    private Drawable colorIcon;
    private @DownloadType int currentAction;
    private WallpaperDialog wallpaperDialog;
    private Intent mReturnIntent;

    private long downloadReference;

    private FirebaseAnalytics mFirebaseAnalytics;

    @BindView((R.id.toolbar_detail)) Toolbar toolbar;
    @BindView(R.id.imgFull) ImageView imgFull;
    @BindView(R.id.imgProfile) ImageView imgProfile;
    @BindView(R.id.btnLike) ImageButton btnLike;
    @BindView(R.id.btnAddToCollection) ImageButton btnAddToCollection;
    @BindView(R.id.tvUser) TextView tvUser;
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.tvLikes) TextView tvLikes;
    @BindView(R.id.tvColor) TextView tvColor;
    @BindView(R.id.tvDownloads) TextView tvDownloads;
    @BindView(R.id.fab_menu) FloatingActionMenu floatingActionMenu;
    @BindView(R.id.fab_download) FloatingActionButton fabDownload;
    @BindView(R.id.fab_wallpaper) FloatingActionButton fabWallpaper;
    @BindView(R.id.fab_stats) FloatingActionButton fabStats;
    @BindView(R.id.fab_info) FloatingActionButton fabInfo;
    @BindView(R.id.detail_content) LinearLayout content;
    @BindView(R.id.detail_progress) ProgressBar loadProgress;

    IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadReference == reference) {
                Cursor cursor = DownloadHelper.getInstance(DetailActivity.this).getDownloadCursor(downloadReference);
                if (cursor != null) {
                    if (DownloadHelper.getInstance(DetailActivity.this).getDownloadStatus(cursor) == DownloadHelper.DownloadStatus.SUCCESS) {
                        File file = new File(DownloadHelper.getInstance(DetailActivity.this).getFilePath(downloadReference));
                        Uri uri = FileProvider.getUriForFile(DetailActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                        if (currentAction == WALLPAPER) {
                            setWallpaper(uri);
                            if (wallpaperDialog != null) wallpaperDialog.setDownloadFinished(true);
                        }
                    }
                    cursor.close();
                }
                if (currentAction == WALLPAPER) {
                    if (wallpaperDialog != null) wallpaperDialog.dismiss();
                }
            }
        }
    };

    PhotoService.OnRequestPhotoDetailsListener mPhotoDetailsRequestListener = new PhotoService.OnRequestPhotoDetailsListener() {
        @Override
        public void onRequestPhotoDetailsSuccess(Call<Photo> call, Response<Photo> response) {
            Log.d(TAG, String.valueOf(response.code()));
            if (response.isSuccessful()) {
                mPhoto = response.body();
                if (mLoadPhotoFromId) {
                    loadInitialPhoto();
                }
                tvUser.setText(getString(R.string.by_author, mPhoto.user.name));
                if (mPhoto.story != null && !Utils.isEmpty(mPhoto.story.title)) {
                    tvTitle.setText(mPhoto.story.title);
                } else {
                    tvTitle.setVisibility(View.GONE);
                }
                if (!Utils.isEmpty(mPhoto.description)) {
                    tvDescription.setText(mPhoto.description);
                } else {
                    tvDescription.setVisibility(View.GONE);
                }
                if (mPhoto.location != null) {
                    if (!Utils.isEmpty(mPhoto.location.title)) {
                        tvLocation.setText(mPhoto.location.title);
                    } else if (mPhoto.location.city != null && mPhoto.location.country != null) {
                        tvLocation.setText(String.format("%s, %s", mPhoto.location.city, mPhoto.location.country));
                    }else if (mPhoto.location.city != null){
                        tvLocation.setText(mPhoto.location.city);
                    } else if (mPhoto.location.country != null){
                        tvLocation.setText(mPhoto.location.country);
                    } else {
                        tvLocation.setVisibility(View.GONE);
                    }
                } else {
                    tvLocation.setVisibility(View.GONE);
                }
                tvDate.setText(mPhoto.created_at.split("T")[0]);
                tvLikes.setText(getString(R.string.likes, NumberFormat.getInstance(Locale.CANADA).format(mPhoto.likes)));
                if (mPhoto.color != null) colorIcon.setColorFilter(Color.parseColor(mPhoto.color), PorterDuff.Mode.SRC_IN);
                tvColor.setText(mPhoto.color);
                tvDownloads.setText(getString(R.string.downloads, NumberFormat.getInstance(Locale.CANADA).format(mPhoto.downloads)));
                mInCollection = mPhoto.current_user_collections.size() > 0;
                updateCollectionButton(mInCollection);
                mPhotoLike = mPhoto.liked_by_user;
                updateHeartButton(mPhotoLike);
                content.setVisibility(View.VISIBLE);
                floatingActionMenu.setVisibility(View.VISIBLE);
                loadProgress.setVisibility(View.GONE);
            } else if (response.code() == 403) {
                Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.cannot_make_anymore_requests), Toast.LENGTH_LONG).show();
            } else if (mPhoto != null){
                mService.requestPhotoDetails(mPhoto.id, this);
            }
        }

        @Override
        public void onRequestPhotoDetailsFailed(Call<Photo> call, Throwable t) {
            Log.d(TAG, t.toString());
            Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.error_loading_photo), Toast.LENGTH_LONG).show();
            finish();
        }
    };

    PhotoService.OnSetLikeListener mSetLikeListener = new PhotoService.OnSetLikeListener() {
        @Override
        public void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
            mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_LIKE_PHOTO, null);
        }

        @Override
        public void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t) { }
    };

    PhotoService.OnReportDownloadListener mReportDownloadListener = new PhotoService.OnReportDownloadListener() {
        @Override
        public void onReportDownloadSuccess(Call<ResponseBody> call, Response<ResponseBody> response) { }

        @Override
        public void onReportDownloadFailed(Call<ResponseBody> call, Throwable t) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        loadPreferences();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mService = PhotoService.getService();

        mPhoto = new Gson().fromJson(getIntent().getStringExtra("Photo"), Photo.class);

        mComingFromCollectionId = getIntent().getIntExtra(CollectionDetailActivity.COLLECTION_DETAIL_ID_FLAG, 0);

        String photoId = getIntent().getStringExtra(DETAIL_ACTIVITY_PHOTO_ID_KEY);

        mReturnIntent = new Intent();

        if (mPhoto != null) {
            loadInitialPhoto();
            mService.requestPhotoDetails(mPhoto.id, mPhotoDetailsRequestListener);
        } else {
            mLoadPhotoFromId = true;
            mService.requestPhotoDetails(photoId, mPhotoDetailsRequestListener);
        }

        floatingActionMenu.setClosedOnTouchOutside(true);
        createCustomAnimation();

        fabDownload.setOnClickListener(onClickListener);
        fabInfo.setOnClickListener(onClickListener);
        fabStats.setOnClickListener(onClickListener);
        fabWallpaper.setOnClickListener(onClickListener);

        colorIcon = getResources().getDrawable(R.drawable.ic_fiber_manual_record_white_18dp, getTheme());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LoginActivity.LOGIN_ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                mService.requestPhotoDetails(mPhoto.id, mPhotoDetailsRequestListener);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }

        unbindDrawables(findViewById(R.id.activity_detail));
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getWindow().setExitTransition(null);
                supportFinishAfterTransition();
                return true;
            case R.id.action_share:
                mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_SHARE_PHOTO, null);
                shareTextUrl();
                return true;
            case R.id.action_view_on_unsplash:
                if(mPhoto != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPhoto.links.html + Resplash.UNSPLASH_UTM_PARAMETERS));
                    if (intent.resolveActivity(getPackageManager()) != null)
                        startActivity(intent);
                    else
                        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        getWindow().setExitTransition(null);
        supportFinishAfterTransition();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab_download:
                    if (Utils.isStoragePermissionGranted(DetailActivity.this) && mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_DOWNLOAD, null);
                        floatingActionMenu.close(true);
                        currentAction = DOWNLOAD;
                        switch (sharedPreferences.getString("download_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, DOWNLOAD);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, DOWNLOAD);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, DOWNLOAD);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, DOWNLOAD);
                                break;
                            case "Full":
                            default:
                                downloadImage(mPhoto.urls.full, DOWNLOAD);
                        }
                    }
                    break;
                case R.id.fab_wallpaper:
                    if (Utils.isStoragePermissionGranted(DetailActivity.this) && mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_SET_WALLPAPER, null);
                        floatingActionMenu.close(true);
                        currentAction = WALLPAPER;
                        switch (sharedPreferences.getString("wallpaper_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, WALLPAPER);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, WALLPAPER);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, WALLPAPER);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, WALLPAPER);
                                break;
                            case "Full":
                            default:
                                downloadImage(mPhoto.urls.full, WALLPAPER);
                        }
                    }
                    break;
                case R.id.fab_info:
                    if (mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_VIEW_PHOTO_INFO, null);
                        floatingActionMenu.close(true);
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setPhoto(mPhoto);
                        infoDialog.show(getFragmentManager(), null);
                    }
                    break;
                case R.id.fab_stats:
                    if (mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_VIEW_PHOTO_STATS, null);
                        floatingActionMenu.close(true);
                        StatsDialog statsDialog = new StatsDialog();
                        statsDialog.setPhoto(mPhoto);
                        statsDialog.show(getFragmentManager(), null);
                    }
                    break;
            }
        }
    };

    private void loadInitialPhoto() {
        if (Resplash.getInstance().getDrawable() != null) {
            imgFull.setImageDrawable(Resplash.getInstance().getDrawable());
            Resplash.getInstance().setDrawable(null);
        } else if (mPhoto.urls != null) {
            String url;
            switch (sharedPreferences.getString("load_quality", "Regular")) {
                case "Raw":
                    url = mPhoto.urls.raw;
                    break;
                case "Full":
                    url = mPhoto.urls.full;
                    break;
                case "Regular":
                    url = mPhoto.urls.regular;
                    break;
                case "Small":
                    url = mPhoto.urls.small;
                    break;
                case "Thumb":
                    url = mPhoto.urls.thumb;
                    break;
                default:
                    url = mPhoto.urls.regular;
            }

            Glide.with(getApplicationContext())
                    .load(url)
                    .apply(new RequestOptions()
                            .priority(Priority.HIGH)
                            .placeholder(new ColorDrawable(ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary))))
                    .transition(withCrossFade())
                    .into(imgFull);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_loading_photo), Toast.LENGTH_SHORT).show();
        }

        if (mPhoto.user.profile_image != null) {
            Glide.with(getApplicationContext())
                    .load(mPhoto.user.profile_image.large)
                    .apply(new RequestOptions().priority(Priority.HIGH))
                    .into(imgProfile);
        }

        imgFull.setOnClickListener(imageOnClickListener);
    }

    public void goToUserProfile(View view){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("username", mPhoto.user.username);
        intent.putExtra("name", mPhoto.user.name);
        startActivity(intent);
    }

    public View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Resplash.getInstance().setPhoto(mPhoto);
            Intent i = new Intent(DetailActivity.this, PreviewActivity.class);
            startActivity(i);
        }
    };

    private void shareTextUrl() {
        if (mPhoto != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unsplash_image));
            share.putExtra(Intent.EXTRA_TEXT, mPhoto.links.html + Resplash.UNSPLASH_UTM_PARAMETERS);

            startActivity(Intent.createChooser(share, getString(R.string.share_via)));
        }
    }

    private void downloadImage(String url, @DownloadType int downloadType) {
        String filename = mPhoto.id + "_" + sharedPreferences.getString("download_quality", "Full") + Resplash.DOWNLOAD_PHOTO_FORMAT;
        if (DownloadHelper.getInstance(this).fileExists(filename)) {
            if (downloadType == WALLPAPER) {
                Uri uri = FileProvider.getUriForFile(DetailActivity.this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        new File(Environment.getExternalStorageDirectory() + Resplash.DOWNLOAD_PATH + filename));
                setWallpaper(uri);

            } else {
                Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.file_exists), Toast.LENGTH_LONG).show();
            }
        } else {
            if (downloadType == WALLPAPER) {
                wallpaperDialog = new WallpaperDialog();
                wallpaperDialog.setListener(() -> DownloadHelper.getInstance(DetailActivity.this).removeDownloadRequest(downloadReference));
                wallpaperDialog.show(getFragmentManager(), null);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.download_started), Toast.LENGTH_SHORT).show();
            }
            mService.reportDownload(mPhoto.id, mReportDownloadListener);
            downloadReference = DownloadHelper.getInstance(this).addDownloadRequest(downloadType, url, filename);
        }
    }

    private void setWallpaper(Uri uri) {
        try {
            Log.d(TAG, "Crop and Set: " + uri.toString());
            Intent wallpaperIntent = WallpaperManager.getInstance(DetailActivity.this).getCropAndSetWallpaperIntent(uri);
            wallpaperIntent.setDataAndType(uri, "image/*");
            wallpaperIntent.putExtra("mimeType", "image/*");
            startActivityForResult(wallpaperIntent, 13451);
            mFirebaseAnalytics.logEvent("set_wallpaper", null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Chooser: " + uri.toString());
            Intent wallpaperIntent = new Intent(Intent.ACTION_ATTACH_DATA);
            wallpaperIntent.setDataAndType(uri, "image/*");
            wallpaperIntent.putExtra("mimeType", "image/*");
            wallpaperIntent.addCategory(Intent.CATEGORY_DEFAULT);
            wallpaperIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            wallpaperIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            wallpaperIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(Intent.createChooser(wallpaperIntent, getString(R.string.set_as_wallpaper)));
            mFirebaseAnalytics.logEvent("set_wallpaper_alternative", null);
        }
    }

    private void loadPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(floatingActionMenu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(floatingActionMenu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(floatingActionMenu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(floatingActionMenu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                floatingActionMenu.getMenuIconView().setImageResource(floatingActionMenu.isOpened()
                        ? R.drawable.ic_expand_less_white_24dp : R.drawable.ic_expand_more_white_24dp);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        floatingActionMenu.setIconToggleAnimatorSet(set);
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    public void likeImage(View view){
        if (AuthManager.getInstance().isAuthorized()) {
            mPhotoLike = !mPhotoLike;
            mService.setLikeForAPhoto(mPhoto.id, mPhotoLike, mSetLikeListener);
            updateHeartButton(mPhotoLike);
            Intent returnIntent = new Intent();
            if (!mPhotoLike) returnIntent.putExtra(UserLikesFragment.PHOTO_UNLIKE_FLAG, true);
            setResult(RESULT_OK, returnIntent);
        } else {
            Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.need_to_log_in), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    public void updateHeartButton(boolean like){
        btnLike.setImageResource(like ? R.drawable.ic_heart_red_24dp : R.drawable.ic_heart_outline_grey_24dp);
    }

    public void addToCollection(View view){
        if (AuthManager.getInstance().isAuthorized()) {
            ManageCollectionsDialog manageCollectionsDialog = new ManageCollectionsDialog();
            manageCollectionsDialog.setPhoto(mPhoto);
            manageCollectionsDialog.setListener(this);
            manageCollectionsDialog.show(getSupportFragmentManager(), null);
        } else {
            Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.need_to_log_in), Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent(this, LoginActivity.class), LoginActivity.LOGIN_ACTIVITY_RESULT_CODE);
        }
    }

    public void updateCollectionButton(boolean inCollection) {
        btnAddToCollection.setImageResource(inCollection ?
                ThemeUtils.getThemeAttrDrawable(this, R.attr.collectionSavedIcon) :
                R.drawable.ic_bookmark_outline_grey_24dp);
    }

    @Override
    public void onCollectionUpdated(@ManageCollectionsDialog.CollectionUpdateType int updateType, Collection collection, @NonNull List<Collection> currentUserCollections) {
        mPhoto.current_user_collections = currentUserCollections;
        mInCollection = currentUserCollections.size() > 0;
        updateCollectionButton(mInCollection);

        if (collection.id == mComingFromCollectionId) {
            if (updateType == ManageCollectionsDialog.CollectionUpdateType.DELETE) {
                mReturnIntent.putExtra(CollectionDetailActivity.PHOTO_REMOVED_FLAG, true);
                mReturnIntent.putExtra("photo_id", mPhoto.id);
            } else {
                mReturnIntent.removeExtra(CollectionDetailActivity.PHOTO_REMOVED_FLAG);
                mReturnIntent.removeExtra("photo_id");
            }
        }

        setResult(RESULT_OK, mReturnIntent);
    }

    @Override
    public void onCollectionCreated() {
        setResult(RESULT_OK, mReturnIntent);
    }
}
