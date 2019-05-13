package com.b_lam.resplash.data.service;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;

import com.b_lam.resplash.data.tools.AutoWallpaperWorker;

/**
 * Used to transition people who had AutoWallpaperService running over to AutoWallpaperWorker.
 * Removed when I feel like everyone has stopped using AutoWallpaperService
 */
public class AutoWallpaperService extends JobService {

    public final static int AUTO_WALLPAPER_JOB_ID = 102134;

    @Override
    public boolean onStartJob(JobParameters params) {
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (jobScheduler != null) {
            jobScheduler.cancel(AUTO_WALLPAPER_JOB_ID);
        }

        AutoWallpaperWorker.Companion.scheduleAutoWallpaperJob(getApplicationContext());

        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobFinished(params, false);
        return false;
    }
}