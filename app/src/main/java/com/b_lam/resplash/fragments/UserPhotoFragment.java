package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.model.User;
import com.google.gson.Gson;

public class UserPhotoFragment extends BasePhotoFragment {

    private User mUser;

    public UserPhotoFragment() {
        // Required empty public constructor
    }

    public static UserPhotoFragment newInstance(String sort) {
        UserPhotoFragment photoFragment = new UserPhotoFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void loadMore(){
        super.loadMore();

        if (mUser != null) {
            mService.requestUserPhotos(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mSort, mRequestPhotoListener);
        } else {
            mImagesProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.VISIBLE);
            mNetworkErrorView.setVisibility(View.GONE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    @Override
    void onPhotoClick(Photo photo, int position) {
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(photo));
        startActivity(i);
    }

    public void setUser(User user){
        this.mUser = user;
    }
}
