package io.github.b_lam.resplash.dialogs;

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

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.data.data.Photo;
import io.github.b_lam.resplash.data.data.PhotoStats;
import io.github.b_lam.resplash.data.service.PhotoService;
import io.github.b_lam.resplash.R;
import io.github.b_lam.resplash.Resplash;
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.stats_dialog, null, false);
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
        if (response.isSuccessful() && response.body() != null) {
            tvLikes.setText(NumberFormat.getInstance(Locale.CANADA).format(response.body().likes) + " Likes");
            tvViews.setText(NumberFormat.getInstance(Locale.CANADA).format(response.body().views) + " Views");
            tvDownloads.setText(NumberFormat.getInstance(Locale.CANADA).format(response.body().downloads) + " Downloads");
            progressBar.setVisibility(View.GONE);
            statsContainer.setVisibility(View.VISIBLE);
        } else if (response.code() == 403) {
            dismiss();
            Toast.makeText(Resplash.getInstance().getApplicationContext(), "Can't make anymore requests.", Toast.LENGTH_LONG).show();
        } else {
            service.requestStats(photo.id, this);
        }
    }

    @Override
    public void onRequestStatsFailed(Call<PhotoStats> call, Throwable t) {
        service.requestStats(photo.id, this);
    }
}
