package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.b_lam.resplash.activities.CollectionDetailActivity;
import com.b_lam.resplash.data.item.CollectionItem;
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.service.CollectionService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener;
import com.mikepenz.fastadapter.ui.items.ProgressItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class CollectionFragment extends Fragment {

    private String TAG = "CollectionFragment";
    private CollectionService mService;
    private FastItemAdapter<CollectionItem> mCollectionAdapter;
    private List<Collection> mCollections;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
    private ItemAdapter mFooterAdapter;
    private int mPage;
    private String mType;
    private GridLayoutManager mGridLayoutManager;

    public CollectionFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(String type) {
        CollectionFragment collectionFragment = new CollectionFragment();

        Bundle args = new Bundle();
        args.putString("type", type);
        collectionFragment.setArguments(args);

        return collectionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mType = getArguments().getString("type", "All");

        mService = CollectionService.getService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);
        mRecyclerView = rootView.findViewById(R.id.fragment_collection_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_collection_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerCollection);

        mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setOnTouchListener((v, event) -> false);
        mRecyclerView.setItemViewCacheSize(5);
        mCollectionAdapter = new FastItemAdapter<>();

        mCollectionAdapter.setOnClickListener((v, adapter, item, position) -> {
            Intent i = new Intent(getContext(), CollectionDetailActivity.class);
            i.putExtra("Collection", new Gson().toJson(item.getModel()));
            startActivity(i);
            return false;
        });

        mFooterAdapter = new ItemAdapter();

        mCollectionAdapter.addAdapter(1, mFooterAdapter);

        mRecyclerView.setAdapter(mCollectionAdapter);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mRecyclerView.post(() -> {
                    mFooterAdapter.clear();
                    ProgressItem progressItem = new ProgressItem();
                    progressItem.setEnabled(false);
                    mFooterAdapter.add(progressItem);
                    loadMore();
                });
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

    private void updateAdapter(List<Collection> collections) {
        for (Collection collection: collections) {
            mCollectionAdapter.add(new CollectionItem(collection));
        }
    }

    public void loadMore(){
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        CollectionService.OnRequestCollectionsListener mCollectionRequestListener = new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mCollections = response.body();
                    mFooterAdapter.clear();
                    CollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }else{
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        switch (mType) {
            case "All":
                mService.requestAllCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
            case "Curated":
                mService.requestCuratedCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
            case "Featured":
                mService.requestFeaturedCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
        }
    }

    private void fetchNew(){
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        CollectionService.OnRequestCollectionsListener mCollectionRequestListener = new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mCollections = response.body();
                    mCollectionAdapter.clear();
                    CollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }else{
                    mImagesProgress.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
                if(mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getContext(), getString(R.string.updated_collections), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        switch (mType) {
            case "All":
                mService.requestAllCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
            case "Curated":
                mService.requestCuratedCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
            case "Featured":
                mService.requestFeaturedCollections(mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
                break;
        }
    }

    public void scrollToTop() {
        if (mRecyclerView != null) {
            if (mGridLayoutManager != null && mGridLayoutManager.findFirstVisibleItemPosition() > 5) {
                mRecyclerView.scrollToPosition(5);
            }
            mRecyclerView.smoothScrollToPosition(0);
        }
    }
}
