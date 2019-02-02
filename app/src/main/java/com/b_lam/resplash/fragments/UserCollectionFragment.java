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
import com.b_lam.resplash.data.model.Collection;
import com.b_lam.resplash.data.model.User;
import com.b_lam.resplash.data.item.CollectionItem;
import com.b_lam.resplash.data.service.CollectionService;
import com.google.gson.Gson;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Response;


public class UserCollectionFragment extends Fragment {

    private String TAG = "CollectionFragment";
    private CollectionService mService;
    private FastItemAdapter<CollectionItem> mCollectionAdapter;
    private List<Collection> mCollections;
    private RecyclerView mImageRecycler;
    private SwipeRefreshLayout mSwipeContainer;
    private ProgressBar mImagesProgress;
    private ConstraintLayout mHttpErrorView;
    private ConstraintLayout mNetworkErrorView;
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
        mImageRecycler = rootView.findViewById(R.id.fragment_user_collection_recycler);
        mImagesProgress = rootView.findViewById(R.id.fragment_user_collection_progress);
        mHttpErrorView = rootView.findViewById(R.id.http_error_view);
        mNetworkErrorView = rootView.findViewById(R.id.network_error_view);
        mSwipeContainer = rootView.findViewById(R.id.swipeContainerUserCollection);

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

    private OnClickListener<CollectionItem> onClickListener = new OnClickListener<CollectionItem>(){
        @Override
        public boolean onClick(View v, IAdapter<CollectionItem> adapter, CollectionItem item, int position) {
            Intent i = new Intent(getContext(), CollectionDetailActivity.class);
            i.putExtra("Collection", new Gson().toJson(item.getModel()));
            i.putExtra(CollectionDetailActivity.USER_COLLECTION_FLAG, true);
            startActivity(i);
            return false;
        }
    };

    public void updateAdapter(List<Collection> collections) {
        for (Collection collection: collections) {
            mCollectionAdapter.add(new CollectionItem(collection));
        }
    }

    public void loadMore(){
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
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
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
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
            mService.requestUserCollections(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
        } else {
            mImagesProgress.setVisibility(View.GONE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.VISIBLE);
            mNetworkErrorView.setVisibility(View.GONE);
            mSwipeContainer.setRefreshing(false);
        }
    }

    public void fetchNew(){
        if(mCollections == null){
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mHttpErrorView.setVisibility(View.GONE);
            mNetworkErrorView.setVisibility(View.GONE);
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
                        mHttpErrorView.setVisibility(View.GONE);
                        mNetworkErrorView.setVisibility(View.GONE);
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
            }

            @Override
            public void onRequestCollectionsFailed(Call<List<Collection>> call, Throwable t) {
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
            mService.requestUserCollections(mUser, mPage, Resplash.DEFAULT_PER_PAGE, mCollectionRequestListener);
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
