package com.b_lam.resplash;

import android.app.WallpaperManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.graphics.Bitmap;
import android.util.Log;

import com.b_lam.resplash.data.service.PhotoService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AutoWallpaperService extends JobService {

    private static final String TAG = "AutoWallpaperService";
    private Target<Bitmap> bitmapTarget;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");
        changeWallpaper(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob");
        return true;
    }

    private void changeWallpaper(final JobParameters params) {

        PhotoService photoService = PhotoService.getService();

        bitmapTarget = Glide.with(this)
                .asBitmap()
                .load("")
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        try {
                            WallpaperManager.getInstance(AutoWallpaperService.this)
                                    .setBitmap(resource);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to set wallpaper");
                            e.printStackTrace();
                        }

                        finish(params);
                    }
                });
    }

    private void finish(final JobParameters params) {
        Glide.with(this).clear(bitmapTarget);
        jobFinished(params, false);
    }
}
