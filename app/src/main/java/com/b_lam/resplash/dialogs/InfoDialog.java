package com.b_lam.resplash.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.b_lam.resplash.R;
import com.b_lam.resplash.data.model.Photo;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;


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

    private Photo mPhoto;

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
        if (mPhoto != null) {
            tvInfoDimensions.setText(mPhoto.width == 0 || mPhoto.height == 0 ? "-----" : getString(R.string.photo_dimensions) + ": " + mPhoto.width + " x " + mPhoto.height);
            tvInfoMake.setText(mPhoto.exif.make == null ? "-----" : getString(R.string.camera_make) + ": " + mPhoto.exif.make);
            tvInfoModel.setText(mPhoto.exif.model == null ? "-----" : getString(R.string.camera_model) + ": " + mPhoto.exif.model);
            tvInfoExposure.setText(mPhoto.exif.exposure_time == null ? "-----" : getString(R.string.exposure_time) + ": " + mPhoto.exif.exposure_time);
            tvInfoAperture.setText(mPhoto.exif.aperture == null ? "-----" : getString(R.string.aperture) + ": " + mPhoto.exif.aperture);
            tvInfoIso.setText(mPhoto.exif.iso == 0 ? "-----" : getString(R.string.iso) + ": " + String.valueOf(mPhoto.exif.iso));
            tvInfoFocalLength.setText(mPhoto.exif.focal_length == null ? "-----" : getString(R.string.focal_length) + ": " + mPhoto.exif.focal_length);
        }
    }

    public void setPhoto(Photo photo) {
        this.mPhoto = photo;
    }
}
