package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.b_lam.resplash.data.data.PhotoDetails;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;


/**
 * Created by Brandon on 10/16/2016.
 */

public class InfoDialog extends DialogFragment {
    @BindView(R.id.tvInfoDimensions) TextView tvInfoDimensions;
    @BindView(R.id.tvInfoMake) TextView tvInfoMake;
    @BindView(R.id.tvInfoModel) TextView tvInfoModel;
    @BindView(R.id.tvInfoExposure) TextView tvInfoExposure;
    @BindView(R.id.tvInfoAperture) TextView tvInfoAperture;
    @BindView(R.id.tvInfoIso) TextView tvInfoIso;
    @BindView(R.id.tvInfoFocalLength) TextView tvInfoFocalLength;

    private PhotoDetails photoDetails;

    DecimalFormat fAperture = new DecimalFormat("0.##");
    DecimalFormat fExposure = new DecimalFormat("0.##########");

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_info, null, false);
        ButterKnife.bind(this, view);
        initData();
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }

    public void initData(){
        tvInfoDimensions.setText(photoDetails.width == 0 || photoDetails.height == 0 ? "-----" : getString(R.string.photo_dimensions) + ": " + photoDetails.width + " x " + photoDetails.height);
        tvInfoMake.setText(photoDetails.exif.make == null ? "-----" : getString(R.string.camera_make) + ": " + photoDetails.exif.make);
        tvInfoModel.setText(photoDetails.exif.model == null ? "-----" : getString(R.string.camera_model) + ": " + photoDetails.exif.model);
        tvInfoExposure.setText(photoDetails.exif.exposure_time == null ? "-----" : getString(R.string.exposure_time) + ": " + photoDetails.exif.exposure_time);
        tvInfoAperture.setText(photoDetails.exif.aperture == null ? "-----" : getString(R.string.aperture) + ": " + photoDetails.exif.aperture);
        tvInfoIso.setText(photoDetails.exif.iso == 0 ? "-----" : getString(R.string.iso) + ": " + String.valueOf(photoDetails.exif.iso));
        tvInfoFocalLength.setText(photoDetails.exif.focal_length == null ? "-----" : getString(R.string.focal_length) + ": " + photoDetails.exif.focal_length);
    }

    public void setPhotoDetails(PhotoDetails photoDetails) {
        this.photoDetails = photoDetails;
    }
}
