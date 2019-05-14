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
import com.b_lam.resplash.activities.UserActivity;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.model.User;
import com.b_lam.resplash.data.tools.AuthManager;
import com.google.gson.Gson;

import static android.app.Activity.RESULT_OK;

public class UserLikesFragment extends BasePhotoFragment {

    public static final int USER_LIKES_UPDATE_CODE = 9279;
    public final static String PHOTO_UNLIKE_FLAG = "PHOTO_UNLIKE_FLAG";

    private User mUser;
    private int mClickedPhotoPosition;

    public UserLikesFragment() { }

    public static UserLikesFragment newInstance(String sort) {
        UserLikesFragment photoFragment = new UserLikesFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mUser != null && mUser.id.equals(AuthManager.getInstance().getID()) && resultCode == RESULT_OK) {
            if (requestCode == USER_LIKES_UPDATE_CODE) {
                if (data.getBooleanExtra(PHOTO_UNLIKE_FLAG, false)) {
                    mItemAdapter.remove(mClickedPhotoPosition);
                    if (getActivity() instanceof UserActivity) {
                        ((UserActivity) getActivity()).getUser().total_likes--;
                        ((UserActivity) getActivity()).setTabTitle(1, getString(R.string.likes, String.valueOf(((UserActivity) getActivity()).getUser().total_likes)));
                    }
                }
            }
        }
    }

    @Override
    public void loadMore(){
        super.loadMore();

        if (mUser != null) {
            mService.requestUserLikes(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mSort, mRequestPhotoListener);
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
        mClickedPhotoPosition = position;
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(photo));
        startActivityForResult(i, USER_LIKES_UPDATE_CODE);
    }

    @Override
    View getView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_likes, container, false);
    }

    public void setUser(User user){
        this.mUser = user;
    }

}
