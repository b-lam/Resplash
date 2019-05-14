package com.b_lam.resplash.fragments;

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
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.service.PhotoService;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public abstract class BasePhotoFragment extends Fragment {

    PhotoService mService;
    PhotoService.OnRequestPhotosListener mRequestPhotoListener;
    int mPage;
    String mSort;
    FastItemAdapter<Photo> mItemAdapter;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeContainer;
    ProgressBar mImagesProgress;
    ConstraintLayout mHttpErrorView;
    ConstraintLayout mNetworkErrorView;

    private final String TAG = "BaseFragment";

    private GridLayoutManager mGridLayoutManager;
    private ItemAdapter mFooterAdapter;
    private List<Photo> mPhotos;
    private int mColumns;

    private OnClickListener<Photo> mOnClickListener = (v, adapter, item, position) -> {
        onPhotoClick(item, position);
        return false;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        String mLayoutType = sharedPreferences.getString("item_layout", "List");
        mSort = getArguments().getString("sort", "latest");
        mColumns = mLayoutType.equals("List") || mLayoutType.equals("Cards") ? 1 : 2;

        mService = PhotoService.getService();

        mRequestPhotoListener = new PhotoService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<List<Photo>> call, Response<List<Photo>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        mPhotos = response.body();
                        mFooterAdapter.clear();
                        updateAdapter(mPhotos);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mHttpErrorView.setVisibility(View.GONE);
                        mNetworkErrorView.setVisibility(View.GONE);
                    } else {
                        mFooterAdapter.clear();
                        mImagesProgress.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.GONE);
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
                    mFooterAdapter.clear();
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        View rootView = getView(inflater, container, savedInstanceState);

        mRecyclerView = rootView.findViewById(R.id.fragment_recycler_view);
        mImagesProgress = rootView.findViewById(R.id.fragment_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.fragment_swipe_refresh_layout);

        mGridLayoutManager = new GridLayoutManager(getContext(), mColumns);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setOnTouchListener((v, event) -> false);
        mRecyclerView.setItemViewCacheSize(5);
        mItemAdapter = new FastItemAdapter<>();

        mItemAdapter.withOnClickListener(mOnClickListener);

        mFooterAdapter = new ItemAdapter<>();

        mItemAdapter.addAdapter(1, mFooterAdapter);

        mRecyclerView.setAdapter(mItemAdapter);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mRecyclerView.post(() -> {
                    mFooterAdapter.clear();
                    mFooterAdapter.add(new ProgressItem().withEnabled(false));
                    loadMore();
                });
            }
        });

        mSwipeContainer.setOnRefreshListener(() -> {
            mPage = 1;
            mPhotos = null;
            mRecyclerView.post(() -> {
                mItemAdapter.clear();
                mFooterAdapter.clear();
                mFooterAdapter.add(new ProgressItem().withEnabled(false));
                loadMore();
            });
        });

        mPage = 1;
        loadMore();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.cancel();
        }
    }

    private void updateAdapter(List<Photo> photos) {
        mItemAdapter.add(photos);
    }

    public void scrollToTop() {
        if (mGridLayoutManager != null && mGridLayoutManager.findFirstVisibleItemPosition() > 5) {
            mRecyclerView.scrollToPosition(5);
        }
        mRecyclerView.smoothScrollToPosition(0);
    }

    public void loadMore() {
        if (mPhotos == null) {
            mImagesProgress.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }
    }

    abstract void onPhotoClick(Photo photo, int position);
    abstract View getView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
}
