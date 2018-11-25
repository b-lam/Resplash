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
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.b_lam.resplash.helpers.DownloadHelper;
import com.b_lam.resplash.util.LocaleUtils;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType;
import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType.DOWNLOAD;
import static com.b_lam.resplash.helpers.DownloadHelper.DownloadType.WALLPAPER;

public class DetailActivity extends AppCompatActivity{

    private static final String TAG = "DetailActivity";
    private boolean mPhotoLike = false;
    private Photo mPhoto;
    private PhotoDetails mPhotoDetails;
    private PhotoService mService;
    private SharedPreferences sharedPreferences;
    private Drawable colorIcon;
    private @DownloadType int currentAction;
    private WallpaperDialog wallpaperDialog;

    private long downloadReference;

    private FirebaseAnalytics mFirebaseAnalytics;

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
                    switch (DownloadHelper.getInstance(DetailActivity.this).getDownloadStatus(cursor)) {
                        case DownloadHelper.DownloadStatus.SUCCESS:
                            File file = new File(DownloadHelper.getInstance(DetailActivity.this).getFilePath(downloadReference));
                            Uri uri = FileProvider.getUriForFile(DetailActivity.this, "com.b_lam.resplash.fileprovider", file);
                            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                            if (currentAction == WALLPAPER) {
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
                                wallpaperDialog.setDownloadFinished(true);
                            }
                            break;
                        default:
                            break;
                    }
                    cursor.close();
                }
                if (currentAction == WALLPAPER) {
                    wallpaperDialog.dismiss();
                }
            }
        }
    };

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
                if (mPhotoDetails.color != null) colorIcon.setColorFilter(Color.parseColor(mPhotoDetails.color), PorterDuff.Mode.SRC_IN);
                tvColor.setText(mPhotoDetails.color);
                tvDownloads.setText(getString(R.string.downloads, NumberFormat.getInstance(Locale.CANADA).format(mPhotoDetails.downloads)));
                mPhotoLike = mPhotoDetails.liked_by_user;
                updateHeartButton(mPhotoLike);
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

    PhotoService.OnSetLikeListener mSetLikeListener = new PhotoService.OnSetLikeListener() {
        @Override
        public void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
            mFirebaseAnalytics.logEvent(Resplash.FIREBASE_EVENT_LIKE_PHOTO, null);
        }

        @Override
        public void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t) {
        }
    };

    PhotoService.OnReportDownloadListener mReportDownloadListener = new PhotoService.OnReportDownloadListener() {
        @Override
        public void onReportDownloadSuccess(Call<ResponseBody> call, Response<ResponseBody> response) {

        }

        @Override
        public void onReportDownloadFailed(Call<ResponseBody> call, Throwable t) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.DetailActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.DetailActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(ThemeUtils.getThemeAttrColor(this, R.attr.menuIconColor), PorterDuff.Mode.SRC_ATOP);
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

            Glide.with(DetailActivity.this)
                    .load(url)
                    .apply(new RequestOptions()
                            .priority(Priority.HIGH)
                            .placeholder(R.drawable.placeholder))
                    .into(imgFull);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_loading_photo), Toast.LENGTH_SHORT).show();
        }

        if (mPhoto.user.profile_image != null) {
            Glide.with(DetailActivity.this)
                    .load(mPhoto.user.profile_image.large)
                    .apply(new RequestOptions().priority(Priority.HIGH))
                    .into(imgProfile);
        }

        colorIcon = getResources().getDrawable(R.drawable.ic_fiber_manual_record_white_18dp, getTheme());

        mService.requestPhotoDetails(mPhoto.id, mPhotoDetailsRequestListener);

        imgFull.setOnClickListener(imageOnClickListener);

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
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPhotoDetails.links.html + Resplash.UNSPLASH_UTM_PARAMETERS));
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
                        Toast.makeText(getApplicationContext(), getString(R.string.download_started), Toast.LENGTH_SHORT).show();
                        currentAction = DOWNLOAD;
                        switch (sharedPreferences.getString("download_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, DOWNLOAD);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, DOWNLOAD);
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
                        wallpaperDialog = new WallpaperDialog();
                        wallpaperDialog.setListener(new WallpaperDialog.WallpaperDialogListener() {
                            @Override
                            public void onCancel() {
                                DownloadHelper.getInstance(DetailActivity.this).removeDownloadRequest(downloadReference);
                            }
                        });
                        wallpaperDialog.show(getFragmentManager(), null);

                        switch (sharedPreferences.getString("wallpaper_quality", "Full")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, WALLPAPER);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, WALLPAPER);
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
    }

    public View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Resplash.getInstance().setPhoto(mPhoto);
            Intent i = new Intent(DetailActivity.this, PreviewActivity.class);
            startActivity(i);
        }
    };

    public void likeImage(View view){
        if(AuthManager.getInstance().isAuthorized()) {
            mPhotoLike = !mPhotoLike;
            mService.setLikeForAPhoto(mPhoto.id, mPhotoLike, mSetLikeListener);
            updateHeartButton(mPhotoLike);
        }else{
            Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.need_to_log_in), Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    public void updateHeartButton(boolean like){
        btnLike.setImageResource(like ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
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

    private void downloadImage(String url, @DownloadType int downloadType){
        mService.reportDownload(mPhoto.id, mReportDownloadListener);
        String filename = mPhoto.id + "_" + sharedPreferences.getString("download_quality", "Full") + Resplash.DOWNLOAD_PHOTO_FORMAT;
        downloadReference = DownloadHelper.getInstance(this).addDownloadRequest(downloadType, url, filename);
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
}
