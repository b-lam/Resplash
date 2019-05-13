package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.data.model.Photo;
import com.google.gson.Gson;

/**
 * Created by Brandon on 10/8/2016.
 */

public class FeaturedFragment extends BasePhotoFragment {

    public FeaturedFragment() {
        // Required empty public constructor
    }

    public static FeaturedFragment newInstance(String sort) {
        FeaturedFragment photoFragment = new FeaturedFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void loadMore(){
        super.loadMore();
        mService.requestCuratedPhotos(mPage, Resplash.DEFAULT_PER_PAGE, mSort, mRequestPhotoListener);
    }

    @Override
    void onPhotoClick(Photo photo, int position) {
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(photo));
        startActivity(i);
    }

    @Override
    View getView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_featured, container, false);
    }
}