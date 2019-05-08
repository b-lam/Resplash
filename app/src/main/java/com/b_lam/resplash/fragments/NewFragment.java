package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.data.model.Photo;
import com.google.gson.Gson;

/**
 * Created by Brandon on 10/8/2016.
 */

public class NewFragment extends BasePhotoFragment {

    public NewFragment() {
        // Required empty public constructor
    }

    public static NewFragment newInstance(String sort) {
        NewFragment newFragment = new NewFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        newFragment.setArguments(args);

        return newFragment;
    }

    @Override
    public void loadMore() {
        super.loadMore();
        mService.requestPhotos(mPage, Resplash.DEFAULT_PER_PAGE, mSort, mRequestPhotoListener);
    }

    @Override
    void onPhotoClick(Photo photo, int position) {
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(photo));
        startActivity(i);
    }
}