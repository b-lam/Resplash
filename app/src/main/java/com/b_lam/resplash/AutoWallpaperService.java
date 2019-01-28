package com.b_lam.resplash;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.service.PhotoService;

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
    public final static String CURRENT_WALLPAPER_ID = "current_wallpaper_id";

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
        return false;
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
                            finish(params);
                        }
                    });
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                finish(params);
            }
        };

        photoService.requestRandomPhotos(null, featured, null, customCategory, null, 1, onRequestPhotosListener);
    }

    private void downloadAndSetWallpaper(Photo photo, PhotoService photoService, JobParameters params) {
        String photoUrl;
        switch (params.getExtras().getString(AUTO_WALLPAPER_QUALITY_KEY, "Full")) {
            case "Raw":
                photoUrl = photo.urls.raw;
                break;
            case "Full":
                photoUrl = photo.urls.full;
                break;
            case "Regular":
                photoUrl = photo.urls.regular;
                break;
            case "Small":
                photoUrl = photo.urls.small;
                break;
            case "Thumb":
                photoUrl = photo.urls.thumb;
                break;
            default:
                photoUrl = photo.urls.full;
        }

        new Thread(() -> {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL(photoUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                WallpaperManager.getInstance(getApplicationContext()).setStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();

                    photoService.reportDownload(photo.id, null);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(CURRENT_WALLPAPER_ID, photo.id);
                    editor.apply();
                }
                finish(params);
            }
        }).start();
    }

    private void finish(final JobParameters params) {
        jobFinished(params, false);
    }
}
