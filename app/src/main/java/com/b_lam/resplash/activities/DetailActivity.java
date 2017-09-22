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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.LikePhotoResult;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.PhotoDetails;
import com.b_lam.resplash.data.service.PhotoService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.dialogs.InfoDialog;
import com.b_lam.resplash.dialogs.StatsDialog;
import com.b_lam.resplash.dialogs.WallpaperDialog;
import com.b_lam.resplash.util.LocaleUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity{

    final String TAG = "DetailActivity";
    private boolean like;
    private Photo mPhoto;
    private PhotoDetails mPhotoDetails;
    private PhotoService mService;
    private SharedPreferences sharedPreferences;
    private Drawable colorIcon;
    private enum ActionType {DOWNLOAD, WALLPAPER}
    private ActionType currentAction;
    private WallpaperDialog wallpaperDialog;

    private long downloadReference;

    private FirebaseAnalytics mFirebaseAnalytics;

    private DownloadManager downloadManager;

    @BindView((R.id.toolbar_detail)) Toolbar toolbar;
    @BindView(R.id.imgFull) ImageView imgFull;
    @BindView(R.id.imgProfile) ImageView imgProfile;
    @BindView(R.id.btnLike) ImageButton btnLike;
    //    @BindView(R.id.btnAddToCollection) ImageButton btnAddToCollection;
    @BindView(R.id.tvUser) TextView tvUser;
    @BindView(R.id.tvLocation) TextView tvLocation;
    @BindView(R.id.tvDate) TextView tvDate;
    @BindView(R.id.tvLikes) TextView tvLikes;
    @BindView(R.id.tvColor) TextView tvColor;
    @BindView(R.id.tvDownloads) TextView tvDownloads;
    @BindView(R.id.progress_download) ProgressBar progressBar;
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
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(reference);
                Cursor cursor = downloadManager.query(query);
                if (cursor.moveToFirst()) {
                    int status  = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, downloadManager.getUriForDownloadedFile(downloadReference)));
                            if (currentAction == ActionType.WALLPAPER) {
                                Uri uri = downloadManager.getUriForDownloadedFile(downloadReference);
                                Log.d(TAG, uri.toString());
                                Intent wallpaperIntent = WallpaperManager.getInstance(DetailActivity.this).getCropAndSetWallpaperIntent(uri);
                                DetailActivity.this.startActivityForResult(wallpaperIntent, 13451);
                                wallpaperDialog.setDownloadFinished(true);
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (currentAction == ActionType.WALLPAPER) {
                    wallpaperDialog.dismiss();
                }
                cursor.close();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mPhoto = new Gson().fromJson(getIntent().getStringExtra("Photo"), Photo.class);

        this.mService = PhotoService.getService();

        loadPreferences();

        floatingActionMenu.setClosedOnTouchOutside(true);
        createCustomAnimation();

        fabDownload.setOnClickListener(onClickListener);
        fabInfo.setOnClickListener(onClickListener);
        fabStats.setOnClickListener(onClickListener);
        fabWallpaper.setOnClickListener(onClickListener);

        if(Resplash.getInstance().getDrawable() != null){
            imgFull.setImageDrawable(Resplash.getInstance().getDrawable());
            Resplash.getInstance().setDrawable(null);
        }else {
            Glide.with(DetailActivity.this)
                    .load(mPhoto.urls.regular)
                    .priority(Priority.HIGH)
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(imgFull);
        }
        Glide.with(DetailActivity.this)
                .load(mPhoto.user.profile_image.large)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(imgProfile);

        colorIcon = getResources().getDrawable(R.drawable.ic_fiber_manual_record_white_18dp, getTheme());

//        btnAddToCollection.setImageDrawable(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).paddingDp(4).colorRes(R.color.md_grey_500));

        PhotoService.OnRequestPhotoDetailsListener mPhotoDetailsRequestListener = new PhotoService.OnRequestPhotoDetailsListener() {
            @Override
            public void onRequestPhotoDetailsSuccess(Call<PhotoDetails> call, Response<PhotoDetails> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (response.isSuccessful()) {
                    mPhotoDetails = response.body();
                    tvUser.setText(getString(R.string.by_author, mPhotoDetails.user.name));
                    if (mPhotoDetails.location != null) {
                        if (mPhotoDetails.location.city != null && mPhotoDetails.location.country != null) {
                            tvLocation.setText(mPhotoDetails.location.city + ", " + mPhotoDetails.location.country);
                        }else if(mPhotoDetails.location.city != null){
                            tvLocation.setText(mPhotoDetails.location.city);
                        }else if(mPhotoDetails.location.country != null){
                            tvLocation.setText(mPhotoDetails.location.country);
                        }
                    } else {
                        tvLocation.setText("-----");
                    }
                    tvDate.setText(mPhotoDetails.created_at.split("T")[0]);
                    tvLikes.setText(getString(R.string.likes, NumberFormat.getInstance(Locale.CANADA).format(mPhotoDetails.likes)));
                    colorIcon.setColorFilter(Color.parseColor(mPhotoDetails.color), PorterDuff.Mode.SRC_IN);
                    tvColor.setText(mPhotoDetails.color);
                    tvDownloads.setText(getString(R.string.downloads, NumberFormat.getInstance(Locale.CANADA).format(mPhotoDetails.downloads)));
                    like = mPhotoDetails.liked_by_user;
                    updateHeartButton(like);
                    content.setVisibility(View.VISIBLE);
                    floatingActionMenu.setVisibility(View.VISIBLE);
                    loadProgress.setVisibility(View.GONE);
                } else if (response.code() == 403) {
                    Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.cannot_make_anymore_requests), Toast.LENGTH_LONG).show();
                } else {
                    mService.requestPhotoDetails(mPhoto.id, this);
                }
            }

            @Override
            public void onRequestPhotoDetailsFailed(Call<PhotoDetails> call, Throwable t) {
                Log.d(TAG, t.toString());
                mService.requestPhotoDetails(mPhoto.id, this);
            }
        };

        mService.requestPhotoDetails(mPhoto.id, mPhotoDetailsRequestListener);

        imgFull.setOnClickListener(imageOnClickListener);

        progressBar.setScaleY(3f);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
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
                getWindow().setExitTransition(null);
                supportFinishAfterTransition();
                return true;
            case R.id.action_share:
                mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_SHARE_PHOTO, null);
                shareTextUrl();
                return true;
            case R.id.action_view_on_unsplash:
                if(mPhotoDetails != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mPhotoDetails.links.html + Resplash.UNSPLASH_UTM_PARAMETERS));
                    startActivity(i);
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
                    if (mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_DOWNLOAD, null);
                        floatingActionMenu.close(true);
                        Toast.makeText(getApplicationContext(), getString(R.string.download_started), Toast.LENGTH_SHORT).show();
                        currentAction = ActionType.DOWNLOAD;
                        switch (sharedPreferences.getString("download_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, ActionType.DOWNLOAD);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, ActionType.DOWNLOAD);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, ActionType.DOWNLOAD);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, ActionType.DOWNLOAD);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, ActionType.DOWNLOAD);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid download quality");
                        }
                    }
                    break;
                case R.id.fab_wallpaper:
                    if (mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_SET_WALLPAPER, null);
                        floatingActionMenu.close(true);
                        currentAction = ActionType.WALLPAPER;
                        wallpaperDialog = new WallpaperDialog();
                        wallpaperDialog.setListener(new WallpaperDialog.WallpaperDialogListener() {
                            @Override
                            public void onCancel() {
                                downloadManager.remove(downloadReference);
                            }
                        });
                        wallpaperDialog.show(getFragmentManager(), null);

                        switch (sharedPreferences.getString("wallpaper_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, ActionType.WALLPAPER);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, ActionType.WALLPAPER);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, ActionType.WALLPAPER);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, ActionType.WALLPAPER);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, ActionType.WALLPAPER);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid wallpaper quality");
                        }
                    }

                    break;
                case R.id.fab_info:
                    if (mPhoto != null) {
                        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_VIEW_PHOTO_INFO, null);
                        floatingActionMenu.close(true);
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setPhotoDetails(mPhotoDetails);
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

    public void goToUserProfile(View view){
        Intent intent = new Intent(this, UserActivity.class);
        intent.putExtra("username", mPhotoDetails.user.username);
        intent.putExtra("name", mPhotoDetails.user.name);
        startActivity(intent);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            startActivity(intent);
//        } else {
//            if(imgProfile.getDrawable() != null) {
//                Resplash.getInstance().setDrawable(imgProfile.getDrawable());
//            }
//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imgProfile, "profileTransition");
//            ActivityCompat.startActivity(this, intent, options.toBundle());
//        }
    }

    public View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Resplash.getInstance().setPhoto(mPhoto);
            Intent i = new Intent(DetailActivity.this, PreviewActivity.class);
            startActivity(i);
        }
    };

    public void updateHeartButton(boolean like){

        if(AuthManager.getInstance().isAuthorized()) {
            if (like) {
                btnLike.setImageResource(R.drawable.ic_heart_red);
            } else {
                btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
            }
        }else{
            btnLike.setVisibility(View.GONE);
        }
    }

    public void likeImage(View view){

        mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_LIKE_PHOTO, null);

        like = !like;

        PhotoService.OnSetLikeListener mSetLikeListener = new PhotoService.OnSetLikeListener() {
            @Override
            public void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
            }

            @Override
            public void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t) {
            }
        };

        mService.setLikeForAPhoto(mPhoto.id, like, mSetLikeListener);
        updateHeartButton(like);
    }

    public void addToCollection(View view){

    }

    private void shareTextUrl() {
        if(mPhoto != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unsplash_image));
            share.putExtra(Intent.EXTRA_TEXT, mPhoto.links.html + Resplash.UNSPLASH_UTM_PARAMETERS);

            startActivity(Intent.createChooser(share, getString(R.string.share_via)));
        }
    }

    private void downloadImage(String url, ActionType actionType){
        String filename = mPhoto.id + "_" + sharedPreferences.getString("download_quality", "Unknown") + Resplash.DOWNLOAD_PHOTO_FORMAT;
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDestinationInExternalPublicDir(Resplash.DOWNLOAD_PATH, filename)
                .setVisibleInDownloadsUi(false)
                .setNotificationVisibility(actionType == ActionType.DOWNLOAD ? DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED : DownloadManager.Request.VISIBILITY_VISIBLE);

        request.allowScanningByMediaScanner();

        downloadReference = downloadManager.enqueue(request);
    }

    private void loadPreferences(){
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
}
