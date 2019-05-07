package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.DetailActivity;
import com.b_lam.resplash.activities.UserActivity;
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.model.User;
import com.b_lam.resplash.data.service.PhotoService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class UserLikesFragment extends Fragment {

    public static final int USER_LIKES_UPDATE_CODE = 9279;
    public final static String PHOTO_UNLIKE_FLAG = "PHOTO_REMOVED_FLAG";

    private final String TAG = "UserLikesFragment";

    private PhotoService mService;
    private FastItemAdapter<Photo> mPhotoAdapter;
    private List<Photo> mPhotos;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
    private ItemAdapter mFooterAdapter;
    private int mPage, mColumns;
    private String mSort;
    private User mUser;
    private int mClickedPhotoPosition;

    public UserLikesFragment() {
    }

    public static UserLikesFragment newInstance(String sort) {
        UserLikesFragment photoFragment = new UserLikesFragment();

        Bundle args = new Bundle();
        args.putString("sort", sort);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        String mLayoutType = sharedPreferences.getString("item_layout", "List");
        mSort = getArguments().getString("sort", "latest");
        if(mLayoutType.equals("List") || mLayoutType.equals("Cards")){
            mColumns = 1;
        }else{
            mColumns = 2;
        }

        mService = PhotoService.getService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_user_likes, container, false);
        mImageRecycler = rootView.findViewById(R.id.fragment_user_likes_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_user_likes_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerUserLikes);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener((v, event) -> false);
        mImageRecycler.setItemViewCacheSize(5);
        mPhotoAdapter = new FastItemAdapter<>();

        mPhotoAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter();

        mPhotoAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mPhotoAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                mFooterAdapter.add(new ProgressItem().withEnabled(false));
                loadMore();
            }
        });

        mSwipeContainer.setOnRefreshListener(this::fetchNew);

        fetchNew();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }
    }

    private OnClickListener<Photo> onClickListener = (v, adapter, item, position) -> {
        mClickedPhotoPosition = position;
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(item));
        startActivityForResult(i, USER_LIKES_UPDATE_CODE);
        return false;
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == USER_LIKES_UPDATE_CODE) {
                if (data.getBooleanExtra(PHOTO_UNLIKE_FLAG, false)) {
                    mPhotoAdapter.remove(mClickedPhotoPosition);
                    if (getActivity() instanceof UserActivity) {
                        ((UserActivity) getActivity()).getUser().total_likes--;
                        ((UserActivity) getActivity()).setTabTitle(1, getString(R.string.likes, String.valueOf(((UserActivity) getActivity()).getUser().total_likes)));
                    }
                }
            }
        }
    }

    public void updateAdapter(List<Photo> photos) {
        mPhotoAdapter.add(photos);
    }

    public void loadMore(){
        if(mPhotos == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }


        PhotoService.OnRequestPhotosListener mPhotoRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (isAdded()) {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        mPhotos = response.body();
                        mFooterAdapter.clear();
                        UserLikesFragment.this.updateAdapter(mPhotos);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mHttpErrorView.setVisibility(View.GONE);
                        mNetworkErrorView.setVisibility(View.GONE);
                    } else {
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.GONE);
                        mHttpErrorView.setVisibility(View.VISIBLE);
                        mNetworkErrorView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                if (isAdded()) {
                    Log.d(TAG, t.toString());
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };

        if (mUser != null) {
            mService.requestUserLikes(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mSort, mPhotoRequestListener);
        } else {
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.VISIBLE);
            mNetworkErrorView.setVisibility(View.GONE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    private void fetchNew(){
        if(mPhotos == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        PhotoService.OnRequestPhotosListener mPhotoRequestListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (isAdded()) {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        mPhotos = response.body();
                        mPhotoAdapter.clear();
                        UserLikesFragment.this.updateAdapter(mPhotos);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mHttpErrorView.setVisibility(View.GONE);
                        mNetworkErrorView.setVisibility(View.GONE);
                    } else {
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.GONE);
                        mHttpErrorView.setVisibility(View.VISIBLE);
                        mNetworkErrorView.setVisibility(View.GONE);
                    }
                    if (mSwipeContainer.isRefreshing()) {
                        Toast.makeText(getContext(), getString(R.string.updated_photos), Toast.LENGTH_SHORT).show();
                        mSwipeContainer.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onRequestPhotosFailed(Call<List<Photo>> call, Throwable t) {
                if (isAdded()) {
                    Log.d(TAG, t.toString());
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };

        if (mUser != null) {
            mService.requestUserLikes(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mSort, mPhotoRequestListener);
        } else {
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.VISIBLE);
            mNetworkErrorView.setVisibility(View.GONE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    public void setUser(User user){
        this.mUser = user;
    }

}
