package com.b_lam.resplash.data.service;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

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

    private static final String TAG = "AutoWallpaperService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        new Thread(() -> changeWallpaper(params)).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob");
        return true;
    }

    private void changeWallpaper(final JobParameters params) {

        final PhotoService photoService = PhotoService.getService();

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
        String photoUrl = getUrlFromQuality(photo,
                params.getExtras().getString(AUTO_WALLPAPER_QUALITY_KEY, "Full"));

        new Thread(() -> {
            URL url = null;
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL(photoUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                WallpaperManager.getInstance(getApplicationContext()).setStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();

                    photoService.reportDownload(photo.id, null);

                    addWallpaperToHistory(photo, params);

                    finish(params, false);
                }

                if (url == null || urlConnection == null || inputStream == null) {
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
