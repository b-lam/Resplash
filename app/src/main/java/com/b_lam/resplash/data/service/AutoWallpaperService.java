package com.b_lam.resplash.data.service;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import com.b_lam.resplash.data.db.Wallpaper;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.repository.WallpaperRepository;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class AutoWallpaperService extends JobService {

    public final static int AUTO_WALLPAPER_JOB_ID = 102134;
    public final static String AUTO_WALLPAPER_CATEGORY_FEATURED_KEY = "auto_wallpaper_category_featured";
    public final static String AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY = "auto_wallpaper_category_custom";
    public final static String AUTO_WALLPAPER_QUALITY_KEY = "auto_wallpaper_quality";
    public final static String AUTO_WALLPAPER_THUMBNAIL_KEY = "auto_wallpaper_thumbnail";
    public final static String AUTO_WALLPAPER_SCREEN_SELECT_KEY = "auto_wallpaper_screen_select";

    private static final String TAG = "AutoWallpaperService";

    private PhotoService photoService;

    @Override
    public boolean onStartJob(JobParameters params) {
        photoService = PhotoService.getService();
        new Thread(() -> changeWallpaper(params)).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (photoService != null) {
            photoService.cancel();
        }
        return true;
    }

    private void changeWallpaper(final JobParameters params) {
        final boolean featured = params.getExtras().getBoolean(AUTO_WALLPAPER_CATEGORY_FEATURED_KEY, false);
        final String customCategory = params.getExtras().getString(AUTO_WALLPAPER_CATEGORY_CUSTOM_KEY, null);

        PhotoService.OnRequestPhotosListener onRequestPhotosListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (response.isSuccessful()) {
                    List<Photo> photos = response.body();
                    if (photos != null && !photos.isEmpty()) {
                        final Photo photo = photos.get(0);
                        downloadAndSetWallpaper(photo, photoService, params);
                    }
                } else {
                    photoService.requestRandomPhotos(null, null, null, null, null, 1, new PhotoService.OnRequestPhotosListener() {
                        @Override
                        public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                            if (response.isSuccessful()) {
                                List<Photo> photos = response.body();
                                if (photos != null && !photos.isEmpty()) {
                                    final Photo photo = photos.get(0);
                                    downloadAndSetWallpaper(photo, photoService, params);
                                }
                            }
                        }

                        @Override
                        public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                            finish(params, true);
                        }
                    });
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                finish(params, true);
            }
        };

        photoService.requestRandomPhotos(null, featured, null, customCategory, null, 1, onRequestPhotosListener);
    }

    private void downloadAndSetWallpaper(Photo photo, PhotoService photoService, JobParameters params) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;

            try {
                final String photoUrl = getUrlFromQuality(photo, params.getExtras().getString(AUTO_WALLPAPER_QUALITY_KEY, "Full"));
                URL url = new URL(photoUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    final int screenSelect = params.getExtras().getInt(AUTO_WALLPAPER_SCREEN_SELECT_KEY, 0);
                    WallpaperManager.getInstance(getApplicationContext()).setStream(inputStream, null,
                            true, screenSelect);
                } else {
                    WallpaperManager.getInstance(getApplicationContext()).setStream(inputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();

                    photoService.reportDownload(photo.id, null);

                    addWallpaperToHistory(photo, params);

                    finish(params, false);
                } else {
                    finish(params, true);
                }
            }
        }).start();
    }

    private void addWallpaperToHistory(Photo photo, JobParameters params) {
        WallpaperRepository repository = new WallpaperRepository(getApplication());

        String thumbnailUrl = getUrlFromQuality(photo,
                params.getExtras().getString(AUTO_WALLPAPER_THUMBNAIL_KEY, "Regular"));

        repository.addWallpaper(new Wallpaper(photo.id, photo.user.name, thumbnailUrl, System.currentTimeMillis()));
    }

    private String getUrlFromQuality(Photo photo, String quality) {
        switch (quality) {
            case "Raw":
                return photo.urls.raw;
            case "Full":
                return photo.urls.full;
            case "Regular":
                return photo.urls.regular;
            case "Small":
                return photo.urls.small;
            case "Thumb":
                return photo.urls.thumb;
            default:
                return photo.urls.regular;
        }
    }

    private void finish(final JobParameters params, final boolean wantsReschedule) {
        jobFinished(params, wantsReschedule);
    }
}
