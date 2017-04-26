package com.b_lam.resplash.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.LikePhotoResult;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.PhotoDetails;
import com.b_lam.resplash.data.service.PhotoService;
import com.b_lam.resplash.data.tools.AuthManager;
import com.b_lam.resplash.dialogs.InfoDialog;
import com.b_lam.resplash.dialogs.StatsDialog;
import com.b_lam.resplash.network.ImageDownloader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    final String TAG = "DetailActivity";
    private boolean like;
    private Photo mPhoto;
    private PhotoDetails mPhotoDetails;
    private PhotoService mService;
    private SharedPreferences sharedPreferences;
    private Drawable colorIcon;
    final static int TYPE_DOWNLOAD = 1;
    final static int TYPE_WALLPAPER = 2;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material, getTheme());
        upArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                    tvUser.setText("Photo by " + mPhotoDetails.user.name);
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
                    tvLikes.setText(NumberFormat.getInstance(Locale.CANADA).format(mPhotoDetails.likes) + " Likes");
                    colorIcon.setColorFilter(Color.parseColor(mPhotoDetails.color), PorterDuff.Mode.SRC_IN);
                    tvColor.setText(mPhotoDetails.color);
                    tvDownloads.setText(NumberFormat.getInstance(Locale.CANADA).format(mPhotoDetails.downloads) + " Downloads");
                    like = mPhotoDetails.liked_by_user;
                    updateHeartButton(like);
                    content.setVisibility(View.VISIBLE);
                    floatingActionMenu.setVisibility(View.VISIBLE);
                } else if (response.code() == 403) {
                    Toast.makeText(Resplash.getInstance().getApplicationContext(), "Can't make anymore requests.", Toast.LENGTH_LONG).show();
                } else {
                    mService.requestPhotoDetails(mPhoto, this);
                }
            }

            @Override
            public void onRequestPhotoDetailsFailed(Call<PhotoDetails> call, Throwable t) {
                Log.d(TAG, t.toString());
                mService.requestPhotoDetails(mPhoto, this);
            }
        };

        mService.requestPhotoDetails(mPhoto, mPhotoDetailsRequestListener);

        imgFull.setOnClickListener(imageOnClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fab_download:
                    if (mPhoto != null) {
                        floatingActionMenu.close(true);
                        Toast.makeText(getApplicationContext(), "Download started", Toast.LENGTH_SHORT).show();
                        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(mPhoto.color), PorterDuff.Mode.MULTIPLY);
                        progressBar.setVisibility(View.VISIBLE);
                        switch (sharedPreferences.getString("download_quality", "Raw")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, TYPE_DOWNLOAD);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, TYPE_DOWNLOAD);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, TYPE_DOWNLOAD);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, TYPE_DOWNLOAD);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, TYPE_DOWNLOAD);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid download quality");
                        }
                    }
                    break;
                case R.id.fab_wallpaper:
                    if (mPhoto != null) {
                        floatingActionMenu.close(true);
                        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor(mPhoto.color), PorterDuff.Mode.MULTIPLY);
                        progressBar.setVisibility(View.VISIBLE);

                        switch (sharedPreferences.getString("wallpaper_quality", "Raw")) {
                            case "Raw":
                                downloadImage(mPhoto.urls.raw, TYPE_WALLPAPER);
                                break;
                            case "Full":
                                downloadImage(mPhoto.urls.full, TYPE_WALLPAPER);
                                break;
                            case "Regular":
                                downloadImage(mPhoto.urls.regular, TYPE_WALLPAPER);
                                break;
                            case "Small":
                                downloadImage(mPhoto.urls.small, TYPE_WALLPAPER);
                                break;
                            case "Thumb":
                                downloadImage(mPhoto.urls.thumb, TYPE_WALLPAPER);
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid wallpaper quality");
                        }
                    }

                    break;
                case R.id.fab_info:
                    if (mPhoto != null) {
                        floatingActionMenu.close(true);
                        InfoDialog infoDialog = new InfoDialog();
                        infoDialog.setPhotoDetails(mPhotoDetails);
                        infoDialog.show(getFragmentManager(), null);
                    }
                    break;
                case R.id.fab_stats:
                    if (mPhoto != null) {
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
                shareTextUrl();
                return true;
            case R.id.action_view_on_unsplash:
                if(mPhotoDetails != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mPhotoDetails.links.html));
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

    public View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Resplash.getInstance().setPhoto(mPhoto);
            Intent i = new Intent(DetailActivity.this, PreviewActivity.class);
            startActivity(i);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }
    }

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

            share.putExtra(Intent.EXTRA_SUBJECT, "Unsplash Image");
            share.putExtra(Intent.EXTRA_TEXT, mPhoto.links.html);

            startActivity(Intent.createChooser(share, "Share via"));
        }
    }

    public void downloadImage(String url, final int type){
        Glide
                .with(getApplicationContext())
                .load(url)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
                        final File myImageFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Pictures" + File.separator + "Resplash"
                                + File.separator + mPhoto.id + "_" + sharedPreferences.getString("download_quality", "Unknown") + "." + mFormat.name().toLowerCase());
                        final Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", myImageFile);
                        ImageDownloader.writeToDisk(myImageFile, resource, new ImageDownloader.OnBitmapSaveListener() {
                            @Override
                            public void onBitmapSaved() {
                                if(type == TYPE_DOWNLOAD) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(contentUri, "image/*");
                                    Toast.makeText(DetailActivity.this, "Image saved", Toast.LENGTH_LONG).show();
                                    sendNotification(intent);
                                }else if (type == TYPE_WALLPAPER) {
                                    Intent intent = WallpaperManager.getInstance(DetailActivity.this).getCropAndSetWallpaperIntent(contentUri);
                                    DetailActivity.this.startActivityForResult(intent, 13451);
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }

                            @Override
                            public void onBitmapSaveError(ImageDownloader.ImageError error) {
                                Toast.makeText(DetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }, mFormat, true);
                    }
                });
    }

    public void sendNotification(Intent intent) {

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_resplash_notification)
                .setContentTitle("Download Wallpaper")
                .setContentText("Download Complete")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(001, mBuilder.build());
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
