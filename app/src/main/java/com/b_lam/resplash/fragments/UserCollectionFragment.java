package com.b_lam.resplash.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.b_lam.resplash.R;
import com.b_lam.resplash.Resplash;
import com.b_lam.resplash.activities.CollectionDetailActivity;
import com.b_lam.resplash.data.data.Collection;
import com.b_lam.resplash.data.data.User;
import com.b_lam.resplash.data.service.CollectionService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Response;
import tr.xip.errorview.ErrorView;

public class UserCollectionFragment extends Fragment {

    private String TAG = "CollectionFragment";
    private CollectionService mService;
    private FastItemAdapter<Collection> mCollectionAdapter;
    private List<Collection> mCollections;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private ItemAdapter mFooterAdapter;
    private int mPage;
    private User mUser;

    public UserCollectionFragment() {
    }

    public static UserCollectionFragment newInstance() {
        UserCollectionFragment collectionFragment = new UserCollectionFragment();
        return collectionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mService = CollectionService.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setRetainInstance(true);

        mPage = 1;

        View rootView = inflater.inflate(R.layout.fragment_user_collection, container, false);
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_user_collection_recycler);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_user_collection_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_user_collection_error_view);
        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainerUserCollection);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener((v, event) -> false);
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
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        CollectionService.OnRequestCollectionsListener mCollectionRequestListener = new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (isAdded()) {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        mCollections = response.body();
                        mFooterAdapter.clear();
                        UserCollectionFragment.this.updateAdapter(mCollections);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mImagesErrorView.setVisibility(View.GONE);
                    } else {
                        mImagesErrorView.setTitle(R.string.error_http);
                        mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.GONE);
                        mImagesErrorView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                if (isAdded()) {
                    Log.d(TAG, t.toString());
                    mImagesErrorView.setRetryVisible(false);
                    mImagesErrorView.setTitle(R.string.error_network);
                    mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };

        if (mUser != null) {
            mService.requestUserCollections(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
        } else {
            mImagesErrorView.setRetryVisible(false);
            mImagesErrorView.setSubtitle(R.string.failed_to_load_profile);
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.VISIBLE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    public void fetchNew(){
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }

        mPage = 1;

        CollectionService.OnRequestCollectionsListener mCollectionRequestListener = new CollectionService.OnRequestCollectionsListener() {
            @Override
            public void onRequestCollectionsSuccess(Call<List<Collection>> call, Response<List<Collection>> response) {
                if (isAdded()) {
                    Log.d(TAG, String.valueOf(response.code()));
                    if (response.code() == 200) {
                        mCollections = response.body();
                        mCollectionAdapter.clear();
                        UserCollectionFragment.this.updateAdapter(mCollections);
                        mPage++;
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mImagesErrorView.setVisibility(View.GONE);
                    } else {
                        mImagesErrorView.setTitle(R.string.error_http);
                        mImagesErrorView.setSubtitle(R.string.error_http_subtitle);
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.GONE);
                        mImagesErrorView.setVisibility(View.VISIBLE);
                    }
                    if (mSwipeContainer.isRefreshing()) {
                        Toast.makeText(getContext(), getString(R.string.updated_collections), Toast.LENGTH_SHORT).show();
                        mSwipeContainer.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
                if (isAdded()) {
                    Log.d(TAG, t.toString());
                    mImagesErrorView.setRetryVisible(false);
                    mImagesErrorView.setTitle(R.string.error_network);
                    mImagesErrorView.setSubtitle(R.string.error_network_subtitle);
                    mImagesProgress.setVisibility(View.GONE);
                    mImageRecycler.setVisibility(View.GONE);
                    mImagesErrorView.setVisibility(View.VISIBLE);
                    mSwipeContainer.setRefreshing(false);
                }
            }
        };

        if (mUser != null) {
            mService.requestUserCollections(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
        } else {
            mImagesErrorView.setRetryVisible(false);
            mImagesErrorView.setSubtitle(R.string.failed_to_load_profile);
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.VISIBLE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    public void setUser(User user){
        this.mUser = user;
    }

}
