package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.data.data.PhotoStats;
import com.b_lam.resplash.data.service.PhotoService;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Brandon on 10/16/2016.
 */

public class StatsDialog extends DialogFragment implements PhotoService.OnRequestStatsListener {

    @BindView(R.id.stats_container) LinearLayout statsContainer;
    @BindView(R.id.tvStatsLikes) TextView tvLikes;
    @BindView(R.id.tvStatsViews) TextView tvViews;
    @BindView(R.id.tvStatsDownloads) TextView tvDownloads;
    @BindView(R.id.stats_progress) ProgressBar progressBar;

    private PhotoService service;
    private Photo photo;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_stats, null, false);
        ButterKnife.bind(this, view);
        this.service = PhotoService.getService();
        service.requestStats(photo.id, this);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (service != null) {
            service.cancel();
        }
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    @Override
    public void onRequestStatsSuccess(Call<PhotoStats> call, Response<PhotoStats> response) {
        if (isAdded()) {
            if (response.isSuccessful() && response.body() != null) {
                tvLikes.setText(getString(R.string.likes, NumberFormat.getInstance(Locale.CANADA).format(response.body().likes)));
                tvViews.setText(getString(R.string.views, NumberFormat.getInstance(Locale.CANADA).format(response.body().views)));
                tvDownloads.setText(getString(R.string.downloads, NumberFormat.getInstance(Locale.CANADA).format(response.body().downloads)));
                progressBar.setVisibility(View.GONE);
                statsContainer.setVisibility(View.VISIBLE);
            } else if (response.code() == 403) {
                dismiss();
                Toast.makeText(Resplash.getInstance().getApplicationContext(), getString(R.string.cannot_make_anymore_requests), Toast.LENGTH_LONG).show();
            } else {
                service.requestStats(photo.id, this);
            }
        }
    }

    @Override
    public void onRequestStatsFailed(Call<PhotoStats> call, Throwable t) {
        if (isAdded()) {
            service.requestStats(photo.id, this);
        }
    }
}
