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
import com.b_lam.resplash.data.model.Photo;
import com.b_lam.resplash.data.model.SearchPhotosResult;
import com.b_lam.resplash.data.service.SearchService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SearchPhotoFragment extends Fragment {

    private final String TAG = "SearchPhotoFragment";

    private SearchService mService;
    private FastItemAdapter<Photo> mItemAdapter;
    private SearchPhotosResult mSearchPhotosResult;
    private List<Photo> mPhotos;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
    private ConstraintLayout mNoResultView;
    private ItemAdapter mFooterAdapter;
    private int mPage, mColumns;
    private String mQuery;

    public SearchPhotoFragment() { }

    public static SearchPhotoFragment newInstance(String query) {
        SearchPhotoFragment photoFragment = new SearchPhotoFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        photoFragment.setArguments(args);

        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Resplash.getInstance());
        String mLayoutType = sharedPreferences.getString("item_layout", "List");
        mColumns = mLayoutType.equals("List") || mLayoutType.equals("Cards") ? 1 : 2;

        mQuery = getArguments().getString("query", null);

        mService = SearchService.getService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        View rootView = inflater.inflate(R.layout.fragment_search_photo, container, false);
        mRecyclerView = rootView.findViewById(R.id.fragment_search_photo_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_search_photo_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerSearchPhoto);
        mNoResultView = rootView.findViewById(R.id.no_results_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setOnTouchListener((v, event) -> false);
        mRecyclerView.setItemViewCacheSize(5);
        mItemAdapter = new FastItemAdapter<>();

        mItemAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter();

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

    private OnClickListener<Photo> onClickListener = (v, adapter, item, position) -> {
        Intent i = new Intent(getContext(), DetailActivity.class);
        i.putExtra("Photo", new Gson().toJson(item));
        startActivity(i);
        return false;
    };

    private void updateAdapter(List<Photo> photos) {
        mItemAdapter.add(photos);
    }

    public void loadMore(){
        if (mPhotos == null && mQuery != null) {
            mImagesProgress.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        SearchService.OnRequestPhotosListener mPhotoRequestListener = new SearchService.OnRequestPhotosListener() {
            @Override
            public void onRequestPhotosSuccess(Call<SearchPhotosResult> call, Response<SearchPhotosResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (isAdded()) {
                    if (response.code() == 200) {
                        mSearchPhotosResult = response.body();
                        mPhotos = mSearchPhotosResult.results;
                        mFooterAdapter.clear();
                        SearchPhotoFragment.this.updateAdapter(mPhotos);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        mHttpErrorView.setVisibility(View.GONE);
                        mNetworkErrorView.setVisibility(View.GONE);
                        if (mItemAdapter.getItemCount() == 0) {
                            mRecyclerView.setVisibility(View.GONE);
                            mNoResultView.setVisibility(View.VISIBLE);
                        }
                    } else {
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
            public void onRequestPhotosFailed(Call<SearchPhotosResult> call, Throwable t) {
                if (isAdded()) {
                    Log.d(TAG, t.toString());
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };

        if (mQuery != null) {
            mService.searchPhotos(mQuery, mPage, Resplash.DEFAULT_PER_PAGE, null, null, mPhotoRequestListener);
            mNoResultView.setVisibility(View.GONE);
        }
    }
}
