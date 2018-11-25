package com.b_lam.resplash.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.data.data.Photo;
import com.b_lam.resplash.util.LocaleUtils;
import com.b_lam.resplash.util.ThemeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class PreviewActivity extends AppCompatActivity {

    @BindView(R.id.preview_image) PhotoView mPhotoView;
    @BindView(R.id.preview_progress) ProgressBar mProgressBar;
    PhotoViewAttacher mAttacher;
    Photo mPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (ThemeUtils.getTheme(this)) {
            case ThemeUtils.Theme.DARK:
                setTheme(R.style.PreviewActivityThemeDark);
                break;
            case ThemeUtils.Theme.BLACK:
                setTheme(R.style.PreviewActivityThemeBlack);
                break;
        }

        super.onCreate(savedInstanceState);

        LocaleUtils.loadLocale(this);

        ThemeUtils.setRecentAppsHeaderColor(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        mPhoto = Resplash.getInstance().getPhoto();

        mAttacher = new PhotoViewAttacher(mPhotoView);

        if(mPhoto != null && mPhoto.urls != null){
            String url;
            switch ( PreferenceManager.getDefaultSharedPreferences(this).getString("load_quality", "Regular")) {
                case "Raw":
                    url = mPhoto.urls.raw;
                    break;
                case "Full":
                    url = mPhoto.urls.full;
                    break;
                case "Regular":
                    url = mPhoto.urls.regular;
                    break;
                case "Small":
                    url = mPhoto.urls.small;
                    break;
                case "Thumb":
                    url = mPhoto.urls.thumb;
                    break;
                default:
                    url = mPhoto.urls.regular;
            }

            Glide.with(this)
                    .load(url)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mPhotoView);
        }else{
            finish();
            Toast.makeText(PreviewActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }
}
