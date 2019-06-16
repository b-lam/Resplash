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
import com.b_lam.resplash.data.model.SearchCollectionsResult;
import com.b_lam.resplash.data.service.SearchService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.scroll.EndlessRecyclerOnScrollListener;
import com.mikepenz.fastadapter.ui.items.ProgressItem;

import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Response;


public class SearchCollectionFragment extends Fragment {

    private String TAG = "SearchCollectionFrag";
    private SearchService mService;
    private FastItemAdapter<CollectionItem> mCollectionAdapter;
    private SearchCollectionsResult mSearchCollections;
    private List<Collection> mCollections;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
    private ConstraintLayout mNoResultView;
    private ItemAdapter mFooterAdapter;
    private int mPage;
    private String mQuery;

    public SearchCollectionFragment() {
    }

    public static SearchCollectionFragment newInstance(String query) {
        SearchCollectionFragment collectionFragment = new SearchCollectionFragment();

        Bundle args = new Bundle();
        args.putString("query", query);
        collectionFragment.setArguments(args);

        return collectionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mQuery = getArguments().getString("query", null);

        mService = SearchService.getService();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ButterKnife.bind(getActivity());

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_search_collection, container, false);
        mImageRecycler = rootView.findViewById(R.id.fragment_search_collection_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_search_collection_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerSearchCollection);
        mNoResultView = rootView.findViewById(R.id.no_results_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener((v, event) -> false);
        mImageRecycler.setItemViewCacheSize(5);
        mCollectionAdapter = new FastItemAdapter<>();

        mCollectionAdapter.setOnClickListener((v, adapter, item, position) -> {
            Intent i = new Intent(getContext(), CollectionDetailActivity.class);
            i.putExtra("Collection", new Gson().toJson(item.getModel()));
            startActivity(i);
            return false;
        });

        mFooterAdapter = new ItemAdapter();

        mCollectionAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mCollectionAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                ProgressItem progressItem = new ProgressItem();
                progressItem.setEnabled(false);
                mFooterAdapter.add(progressItem);
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

    private void updateAdapter(List<Collection> collections) {
        for (Collection collection: collections) {
            mCollectionAdapter.add(new CollectionItem(collection));
        }
    }

    public void loadMore(){
        if(mSearchCollections == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        SearchService.OnRequestCollectionsListener mCollectionRequestListener = new SearchService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<SearchCollectionsResult> call, Response<SearchCollectionsResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (response.code() == 200) {
                    mSearchCollections = response.body();
                    mCollections = mSearchCollections.results;
                    mFooterAdapter.clear();
                    SearchCollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                    if (mCollectionAdapter.getItemCount() == 0) {
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<SearchCollectionsResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if (mQuery != null) {
            mService.searchCollections(mQuery, mPage, 30, mCollectionRequestListener);
            mNoResultView.setVisibility(View.GONE);
        }

    }

    private void fetchNew(){
        if (mSearchCollections == null && mQuery != null) {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        SearchService.OnRequestCollectionsListener mCollectionRequestListener = new SearchService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<SearchCollectionsResult> call, Response<SearchCollectionsResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if (response.code() == 200) {
                    mSearchCollections = response.body();
                    mCollections = mSearchCollections.results;
                    mCollectionAdapter.clear();
                    SearchCollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mHttpErrorView.setVisibility(View.GONE);
                    mNetworkErrorView.setVisibility(View.GONE);
                    if (mCollectionAdapter.getItemCount() == 0) {
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultView.setVisibility(View.VISIBLE);
                    }
                } else {
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mHttpErrorView.setVisibility(View.VISIBLE);
                    mNetworkErrorView.setVisibility(View.GONE);
                }
                if (mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getContext(), getString(R.string.updated_collections), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<SearchCollectionsResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mHttpErrorView.setVisibility(View.GONE);
                mNetworkErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if (mQuery != null) {
            mService.searchCollections(mQuery, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
            mNoResultView.setVisibility(View.GONE);
        }

    }

}
