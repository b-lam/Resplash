package com.b_lam.resplash.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.b_lam.resplash.activities.CollectionDetailActivity;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.SearchCollectionsResult;
import com.b_lam.resplash.data.service.SearchService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import butterknife.ButterKnife;
import com.b_lam.resplash.R;
import retrofit2.Call;
import retrofit2.Response;
import tr.xip.errorview.ErrorView;


public class SearchCollectionFragment extends Fragment {

    private String TAG = "SearchCollectionFrag";
    private SearchService mService;
    private FastItemAdapter<Collection> mCollectionAdapter;
    private SearchCollectionsResult mSearchCollections;
    private List<Collection> mCollections;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private TextView mNoResultTextView;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ButterKnife.bind(getActivity());

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_search_collection, container, false);
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_search_collection_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_search_collection_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_search_collection_error_view);
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerSearchCollection);
        mNoResultTextView = (TextView) rootView.findViewById(R.id.collection_no_results);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mImageRecycler.setItemViewCacheSize(5);
        mCollectionAdapter = new FastItemAdapter<>();

        mCollectionAdapter.withOnClickListener(onClickListener);

        mFooterAdapter = new ItemAdapter();

        mCollectionAdapter.addAdapter(1, mFooterAdapter);

        mImageRecycler.setAdapter(mCollectionAdapter);

        mImageRecycler.addOnScrollListener(new EndlessRecyclerOnScrollListener(mFooterAdapter) {
            @Override
            public void onLoadMore(int currentPage) {
                mFooterAdapter.clear();
                mFooterAdapter.add(new ProgressItem().withEnabled(false));
                loadMore();
            }
        });

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNew();
            }
        });

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

    private OnClickListener<Collection> onClickListener = new OnClickListener<Collection>(){
        @Override
        public boolean onClick(View v, IAdapter<Collection> adapter, Collection item, int position) {
            Intent i = new Intent(getContext(), CollectionDetailActivity.class);
            i.putExtra("Collection", new Gson().toJson(item));
            startActivity(i);
            return false;
        }
    };

    public void updateAdapter(List<Collection> collections) {
        mCollectionAdapter.add(collections);
    }

    public void loadMore(){
        if(mSearchCollections == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        SearchService.OnRequestCollectionsListener mCollectionRequestListener = new SearchService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<SearchCollectionsResult> call, Response<SearchCollectionsResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchCollections = response.body();
                    mCollections = mSearchCollections.results;
                    mFooterAdapter.clear();
                    SearchCollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                    if(mCollectionAdapter.getItemCount() == 0){
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultTextView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mImagesErrorView.setTitle(R.string.error_http);
                    mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<SearchCollectionsResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.setRetryVisible(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchCollections(mQuery, mPage, mCollectionRequestListener);
            mNoResultTextView.setVisibility(View.GONE);
        }

    }

    public void fetchNew(){
        if(mSearchCollections == null && mQuery != null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        SearchService.OnRequestCollectionsListener mCollectionRequestListener = new SearchService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<SearchCollectionsResult> call, Response<SearchCollectionsResult> response) {
                Log.d(TAG, String.valueOf(response.code()));
                if(response.code() == 200) {
                    mSearchCollections = response.body();
                    mCollections = mSearchCollections.results;
                    mCollectionAdapter.clear();
                    SearchCollectionFragment.this.updateAdapter(mCollections);
                    mPage++;
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.VISIBLE);
                    mImagesErrorView.setVisibility(View.GONE);
                    if(mCollectionAdapter.getItemCount() == 0){
                        mImageRecycler.setVisibility(View.GONE);
                        mNoResultTextView.setVisibility(View.VISIBLE);
                    }
                }else{
                    mImagesErrorView.setTitle(R.string.error_http);
                    mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                }
                if(mSwipeContainer.isRefreshing()) {
                    Toast.makeText(getContext(), getString(R.string.updated_collections), Toast.LENGTH_SHORT).show();
                    mSwipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<SearchCollectionsResult> call, Throwable t) {
                Log.d(TAG, t.toString());
                mImagesErrorView.setRetryVisible(false);
                mImagesErrorView.setTitle(R.string.error_network);
                mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mSwipeContainer.setRefreshing(false);
            }
        };

        if(mQuery != null) {
            mService.searchCollections(mQuery, mPage, mCollectionRequestListener);
            mNoResultTextView.setVisibility(View.GONE);
        }

    }

}
